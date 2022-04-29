package com.zzsong.monitor.center.infrastructure.edge

import cn.idealframework.transmission.exception.ResourceNotFoundException
import com.zzsong.monitor.center.configure.MonitorCenterProperties
import com.zzsong.monitor.center.domain.model.cluster.ClusterRepository
import com.zzsong.monitor.common.constants.ConnectType
import com.zzsong.monitor.prometheus.ReactorPrometheusClient
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * @author 宋志宗 on 2022/4/25
 */
@Component
class EdgeConnector(
  properties: MonitorCenterProperties,
  private val clusterRepository: ClusterRepository,
  private val redisTemplate: ReactiveStringRedisTemplate,
  reactiveRedisMessageListenerContainer: ReactiveRedisMessageListenerContainer
) : SmartInitializingSingleton, DisposableBean {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(EdgeConnector::class.java)
  }

  private val queue = ArrayBlockingQueue<Boolean>(1)
  private val destroyed = AtomicBoolean(false)
  private var lastRefresh = AtomicLong(System.currentTimeMillis())
  private val automaticRefreshCycle = Duration.ofMinutes(10).toMillis()
  private val refreshTopicName = "${properties.cachePrefix}:edge_connector:refresh"
  private val redisListener = reactiveRedisMessageListenerContainer
    .receive(PatternTopic(refreshTopicName))
    .doOnNext {
      log.info("监听到Redis刷新客户端缓存通知")
      queue.offer(true)
    }.subscribe()
  private var edgeClientCache = HashMap<String, EdgeClient>()
  private var prometheusClientCache = HashMap<String, ReactorPrometheusClient>()

  init {
    Thread {
      while (!destroyed.get()) {
        val poll = queue.poll(5, TimeUnit.SECONDS)
        if (poll != null) {
          runBlocking { refresh() }
          continue
        }
        // 如果长时间没有刷新客户端缓存, 则自动执行一次刷新操作
        if (System.currentTimeMillis() - lastRefresh.get() > automaticRefreshCycle) {
          queue.offer(true)
        }
      }
    }.also { it.isDaemon = true }.start()
  }

  suspend fun <T> execute(code: String, block: suspend (EdgeClient) -> T): T {
    val client = getEdgeClient(code)
    return block.invoke(client)
  }

  suspend fun <T> execProm(code: String, block: suspend (ReactorPrometheusClient) -> T): T {
    val prometheusClient = getPrometheusClient(code)
    return block.invoke(prometheusClient)
  }

  /** 发布刷新广播通知, 用于通知所有节点刷新客户端缓存 */
  suspend fun pubRefreshBroadcast() {
    redisTemplate.convertAndSend(refreshTopicName, "true").subscribe()
  }

  private suspend fun refresh() {
    lastRefresh.set(System.currentTimeMillis())
    val all = clusterRepository.findAll()
    val edgeClientCache = HashMap<String, EdgeClient>()
    val prometheusClientCache = HashMap<String, ReactorPrometheusClient>()
    for (clusterDo in all) {
      val code = clusterDo.code
      val connectType = clusterDo.connectType
      if (connectType == ConnectType.DIRECT) {
        val address = clusterDo.address
        // 加入边缘节点客户端缓存
        val edgeClient = EdgeClientImpl()
        edgeClientCache[code] = edgeClient
        // 加入prometheus客户端缓存
        val prometheusClient = ReactorPrometheusClient.newInstance(
          "$address/edge/monitor/prometheus",
          Duration.ofSeconds(5)
        )
        prometheusClientCache[code] = prometheusClient
      }
    }
    this.edgeClientCache = edgeClientCache
    this.prometheusClientCache = prometheusClientCache
  }

  /** 通过边缘节点id获取客户端对象 */
  private fun getEdgeClient(code: String): EdgeClient {
    return edgeClientCache[code] ?: kotlin.run {
      log.warn("边缘节点: {} 没有客户端缓存对象", code)
      throw ResourceNotFoundException("找不到缓存的客户端对象")
    }
  }

  private fun getPrometheusClient(code: String): ReactorPrometheusClient {
    return prometheusClientCache[code] ?: kotlin.run {
      log.warn("边缘节点: {} 没有客户端缓存对象", code)
      throw ResourceNotFoundException("找不到缓存的客户端对象")
    }
  }

  override fun afterSingletonsInstantiated() {
    runBlocking { refresh() }
  }

  override fun destroy() {
    if (destroyed.get()) {
      return
    }
    destroyed.set(true)
    redisListener.dispose()
  }
}

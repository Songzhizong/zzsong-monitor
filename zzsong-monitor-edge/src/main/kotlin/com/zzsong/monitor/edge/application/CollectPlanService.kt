package com.zzsong.monitor.edge.application

import com.zzsong.monitor.edge.configure.MonitorEdgeProperties
import com.zzsong.monitor.edge.infrastructure.client.CenterClient
import com.zzsong.monitor.edge.infrastructure.collect.CollectorManagerRegistry
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 采集计划服务
 *
 * @author 宋志宗 on 2022/4/22
 */
@Service
class CollectPlanService(
  private val centerClient: CenterClient,
  private val properties: MonitorEdgeProperties,
  private val redisTemplate: ReactiveStringRedisTemplate,
  private val collectorManagerRegistry: CollectorManagerRegistry,
  reactiveRedisMessageListenerContainer: ReactiveRedisMessageListenerContainer
) : DisposableBean {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(CollectPlanService::class.java)
  }

  /** 长度为1的队列用于防止无意义的刷新操作 */
  private val queue = ArrayBlockingQueue<Boolean>(1)

  /** 每隔60秒 尝试获取最近发生变更的采集计划 */
  private val autoUpdateInterval = Duration.ofSeconds(60).toMillis()

  /** 每隔10分钟 全量刷新当前集群的采集计划 */
  private val autoRefreshInterval = Duration.ofMinutes(10).toMillis()
  private val destroyed = AtomicBoolean(false)

  private val updateTopicName = "${properties.cachePrefix}:collect_plan:update_notice"
  private val redisListener = reactiveRedisMessageListenerContainer
    .receive(PatternTopic(updateTopicName))
    .doOnNext {
      log.info("监听到Redis更新采集计划通知")
      queue.offer(true)
    }.subscribe()


  /** 最近一次全量刷新采集计划的执行时间 */
  private var lastRefresh = System.currentTimeMillis()

  /** 最近一次更新采集计划的执行时间, 全量刷新也算在内 */
  private var lastUpdate = System.currentTimeMillis()

  init {
    Thread {
      while (!destroyed.get()) {
        val currentTimeMillis = System.currentTimeMillis()
        val poll = queue.poll(5, TimeUnit.SECONDS)
        if (poll != null) {
          @Suppress("OPT_IN_USAGE")
          runBlocking {
            try {
              if (poll) {
                doUpdatePlan()
              } else {
                doRefreshPlan()
              }
            } catch (e: Exception) {
              log.info("更新或者刷新采集计划出现异常: ", e)
            }
          }
          // 如果长时间没有收到更新通知, 则自动触发更新操作
        } else {
          if (currentTimeMillis - lastUpdate > autoUpdateInterval) {
            queue.offer(true)
          }
        }
        // 每隔一段时间全量刷新采集计划
        if (currentTimeMillis - lastRefresh > autoRefreshInterval) {
          queue.offer(false)
        }
      }
    }.start()
  }

  /**
   * 更新通知
   *
   * 如果当前边缘集群的采集计划发生了变更, 则中心节点会通过接口向边缘集群发送通知.
   * 由于中心节点只能通知到边缘集群中的某一个节点, 因此这里需要向Redis某个主题发送广播.
   * 边缘集群内所有的节点通过订阅广播消息, 达到节点内主动更新采集计划的目的.
   *
   * @author 宋志宗 on 2022/4/23
   */
  suspend fun collectPlanChanged() {
    redisTemplate.convertAndSend(updateTopicName, "true").awaitSingleOrNull()
  }

  /**
   * 全量刷新采集计划
   *
   * @author 宋志宗 on 2022/4/23
   */
  @DelicateCoroutinesApi
  private suspend fun doRefreshPlan() {
    val cluster = properties.cluster
    val timestamp = System.currentTimeMillis() - 1
    this.lastRefresh = timestamp
    this.lastUpdate = timestamp
    val allPlan = centerClient.findCollectPlan(cluster)
    if (allPlan.isEmpty()) {
      return
    }
    log.info("发现采集计划 {}条", allPlan.size)
    val group = allPlan.groupBy { it.type }
    for ((type, planList) in group) {
      val manager = collectorManagerRegistry.getIfPresent(type)
      if (manager == null) {
        log.error("找不到采集管理器: {}", type)
        continue
      }
      manager.refresh(planList)
    }
  }

  /**
   * 增量更新采集计划
   *
   * @author 宋志宗 on 2022/4/23
   */
  @DelicateCoroutinesApi
  private suspend fun doUpdatePlan() {
    val cluster = properties.cluster
    val updatedTimeGte = this.lastUpdate
    this.lastUpdate = System.currentTimeMillis() - 1
    val allUpdatePlan = centerClient.findRecentlyModifiedCollectPlan(cluster, updatedTimeGte)
    if (allUpdatePlan.isEmpty()) {
      return
    }
    log.info("变更采集计划 {}条", allUpdatePlan.size)
    val group = allUpdatePlan.groupBy { it.type }
    for ((type, planList) in group) {
      val manager = collectorManagerRegistry.getIfPresent(type)
      if (manager == null) {
        log.error("找不到采集管理器: {}", type)
        continue
      }
      manager.update(planList)
    }
  }

  @Suppress("OPT_IN_USAGE")
  fun init() {
    runBlocking { doRefreshPlan() }
  }

  override fun destroy() {
    if (destroyed.get()) {
      return
    }
    destroyed.set(true)
    redisListener.dispose()
  }
}

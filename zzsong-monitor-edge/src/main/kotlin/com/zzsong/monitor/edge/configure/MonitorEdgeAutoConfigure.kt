package com.zzsong.monitor.edge.configure

import cn.idealframework.lang.StringUtils
import com.zzsong.monitor.edge.infrastructure.client.CenterClient
import com.zzsong.monitor.edge.infrastructure.client.CenterClientImpl
import com.zzsong.monitor.prometheus.ReactorPrometheusClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer

/**
 * @author 宋志宗 on 2022/3/19
 */
@ComponentScan("com.zzsong.monitor.edge")
@EnableConfigurationProperties(value = [MonitorEdgeProperties::class])
class MonitorEdgeAutoConfigure(
  private val properties: MonitorEdgeProperties
) : SmartInitializingSingleton {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(MonitorEdgeAutoConfigure::class.java)
  }

  @Bean
  fun centerClient(): CenterClient {
    val centerBaseUrl = properties.centerBaseUrl
    if (StringUtils.isBlank(centerBaseUrl)) {
      val message = "未配置中心节点基础访问地址: monitor.edge.center-base-url"
      log.error(message)
      throw RuntimeException(message)
    }
    return CenterClientImpl(centerBaseUrl)
  }

  @Bean("monitorEdgeReactiveRedisMessageListenerContainer")
  fun reactiveRedisMessageListenerContainer(
    reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory
  ): ReactiveRedisMessageListenerContainer {
    return ReactiveRedisMessageListenerContainer(reactiveRedisConnectionFactory)
  }

  @Bean
  fun prometheusClient(): ReactorPrometheusClient? {
    val read = properties.read
    val prometheus = read.prometheus
    val baseUrl = prometheus.baseUrl
    if (StringUtils.isBlank(baseUrl)) {
      log.info("未配置prometheus读取地址")
      return null
    }
    val timeout = prometheus.timeout
    return ReactorPrometheusClient.newInstance(baseUrl, timeout)
  }

  override fun afterSingletonsInstantiated() {
    // 校验是否正确配置了边缘集群的名称
    val clusterName = properties.cluster
    if (StringUtils.isBlank(clusterName)) {
      val message = "未配置集群名称: monitor.edge.cluster-name"
      log.error(message)
      throw RuntimeException(message)
    }
    // 校验是否正确配置了中心节点的基础访问地址
    val centerBaseUrl = properties.centerBaseUrl
    if (StringUtils.isBlank(centerBaseUrl)) {
      val message = "未配置中心节点基础访问地址: monitor.edge.center-base-url"
      log.error(message)
      throw RuntimeException(message)
    }
  }
}


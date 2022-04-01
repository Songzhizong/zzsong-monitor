package com.zzsong.monitor.edge.configure

import com.zzsong.monitor.edge.configure.properties.MonitorEdgeProperties
import com.zzsong.monitor.edge.infrastructure.center.CenterClient
import com.zzsong.monitor.edge.infrastructure.center.CenterClientImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

/**
 * @author 宋志宗 on 2022/3/19
 */
@ComponentScan("com.zzsong.monitor.edge")
class MonitorEdgeAutoConfigure(
  private val properties: MonitorEdgeProperties
) : SmartInitializingSingleton {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(MonitorEdgeAutoConfigure::class.java)
  }

  @Bean
  fun centerClient(): CenterClient {
    return CenterClientImpl(properties.centerBaseUrl)
  }

  override fun afterSingletonsInstantiated() {
    // 校验是否正确配置了边缘集群的名称
    val clusterName = properties.clusterName
    assert(clusterName.isNotBlank()) {
      val message = "未配置集群名称: monitor.edge.cluster-name"
      log.error(message)
      message
    }
    // 校验是否正确配置了中心节点的基础访问地址
    val centerBaseUrl = properties.centerBaseUrl
    assert(centerBaseUrl.isNotBlank()) {
      val message = "未配置中心节点的基础访问地址: monitor.edge.center-base-url"
      log.error(message)
      message
    }
  }
}


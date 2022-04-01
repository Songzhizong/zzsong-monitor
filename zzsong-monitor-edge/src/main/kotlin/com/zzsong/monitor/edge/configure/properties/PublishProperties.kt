package com.zzsong.monitor.edge.configure.properties

import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * 指标信息发布配置
 *
 * @author 宋志宗 on 2022/3/19
 */
class PublishProperties {

  @NestedConfigurationProperty
  val kafka = KafkaPublishProperties()

  @NestedConfigurationProperty
  val http = HttpPublishProperties()
}

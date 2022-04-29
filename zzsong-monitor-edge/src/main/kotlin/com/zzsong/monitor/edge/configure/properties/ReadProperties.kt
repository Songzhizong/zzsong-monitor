package com.zzsong.monitor.edge.configure.properties

import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * 指标读取配置
 *
 * @author 宋志宗 on 2022/4/22
 */
class ReadProperties {

  /** prometheus读取配置 */
  @NestedConfigurationProperty
  var prometheus = PrometheusReadProperties()
}

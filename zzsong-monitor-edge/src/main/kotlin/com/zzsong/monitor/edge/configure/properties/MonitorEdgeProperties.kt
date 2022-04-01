package com.zzsong.monitor.edge.configure.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * @author 宋志宗 on 2022/3/26
 */
@Component
@ConfigurationProperties("monitor.edge")
class MonitorEdgeProperties {
  /** 集群名称 */
  var clusterName = ""

  /** 中心节点的基础访问地址 */
  var centerBaseUrl = ""

  /** prometheus配置信息 */
  val prometheus = PrometheusProperties()

  /** 指标数据发布配置 */
  val publish = PublishProperties()
}

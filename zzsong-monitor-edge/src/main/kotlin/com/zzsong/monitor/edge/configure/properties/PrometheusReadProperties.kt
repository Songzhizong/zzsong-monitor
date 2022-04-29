package com.zzsong.monitor.edge.configure.properties

import java.time.Duration

/**
 * prometheus相关配置
 *
 * @author 宋志宗 on 2022/3/19
 */
class PrometheusReadProperties {
  /** Request timeout */
  var timeout: Duration = Duration.ofSeconds(3)

  /** 读取地址 */
  var baseUrl: String = ""
}

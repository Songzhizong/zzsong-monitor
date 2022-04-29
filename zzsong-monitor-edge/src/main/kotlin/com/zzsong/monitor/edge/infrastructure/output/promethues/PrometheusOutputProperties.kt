package com.zzsong.monitor.edge.infrastructure.output.promethues

import java.time.Duration

/**
 * @author 宋志宗 on 2022/4/22
 */
class PrometheusOutputProperties {

  var timeout: Duration = Duration.ofSeconds(3)

  /** url地址 */
  var url = "http://127.0.0.1:9090/api/v1/write"
}

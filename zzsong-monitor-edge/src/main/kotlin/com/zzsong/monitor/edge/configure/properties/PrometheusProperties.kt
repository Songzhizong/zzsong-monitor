package com.zzsong.monitor.edge.configure.properties

import java.time.Duration

/**
 * prometheus相关配置
 *
 * @author 宋志宗 on 2022/3/19
 */
class PrometheusProperties {
  /** Basic auth username */
  var basicAuthUser: String = ""

  /** Basic auth password */
  var basicAuthPass: String = ""

  /** Request timeout */
  var timeout: Duration = Duration.ofSeconds(3)

  /** 读取地址 */
  var readBaseUrl: String = "http://127.0.0.1:9090"

  /** 写入地址列表, 配置多个地址以支持多写 */
  var writeUrls: Set<String> = setOf("http://127.0.0.1:9090/api/v1/write")
}

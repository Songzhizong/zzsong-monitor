package com.zzsong.monitor.center.port.http

import com.zzsong.monitor.common.pojo.Metric
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 指标数据接口
 *
 * @author 宋志宗 on 2022/3/26
 */
@RestController
@RequestMapping("/monitor/metric")
class MetricController {

  /**
   * 指标数据上报
   *
   * @author 宋志宗 on 2022/3/26
   */
  @PostMapping("/report")
  suspend fun metricReport(
    cluster: String,
    @RequestBody metrics: List<Metric>
  ) {

  }
}

package com.zzsong.monitor.edge.port.http

import cn.idealframework.transmission.Result
import com.zzsong.monitor.edge.application.CollectPlanService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 采集计划接口
 *
 * @author 宋志宗 on 2022/4/23
 */
@RestController
@RequestMapping("/edge/monitor/collect_plan")
class CollectPlanController(private val collectPlanService: CollectPlanService) {

  /**
   * 采集计划变更通知
   *
   * @author 宋志宗 on 2022/4/23
   */
  @GetMapping("/changed_notice")
  suspend fun updateNotice(): Result<Void> {
    collectPlanService.collectPlanChanged()
    return Result.success()
  }
}

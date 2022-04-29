package com.zzsong.monitor.center.port.http

import cn.idealframework.lang.StringUtils
import cn.idealframework.transmission.Result
import cn.idealframework.util.Asserts
import com.zzsong.monitor.center.application.AlertRuleService
import com.zzsong.monitor.center.args.CreateAlertRuleArgs
import com.zzsong.monitor.center.args.SelectivityUpdateAlertRuleArgs
import com.zzsong.monitor.common.pojo.AlertRule
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 告警规则管理
 *
 * @author 宋志宗 on 2022/4/25
 */
@RestController
@RequestMapping("/monitor/alert/rule")
class AlertRuleController(private val alertRuleService: AlertRuleService) {

  /**
   * 新建告警规则
   *
   * @author 宋志宗 on 2022/4/25
   */
  @PostMapping("/create")
  suspend fun create(
    @RequestBody(required = false)
    args: CreateAlertRuleArgs?
  ): Result<AlertRule> {
    Asserts.nonnull(args, "body为空");args!!
    val alertRuleDo = alertRuleService.create(args)
    val alertRule = alertRuleDo.toAlertRule()
    return Result.success(alertRule)
  }

  /**
   * 选择性更新规则字段
   *
   * @author 宋志宗 on 2022/4/25
   */
  @PostMapping("/update/selectivity")
  suspend fun selectivityUpdate(
    ids: String?,
    @RequestBody(required = false)
    args: SelectivityUpdateAlertRuleArgs?
  ): Result<Void> {
    if (StringUtils.isBlank(ids)) {
      return Result.success()
    }
    Asserts.nonnull(args, "body为空");args!!
    val idSet = StringUtils.split(ids, ",")
      .mapTo(HashSet()) { it.toLong() }
    alertRuleService.selectivityUpdate(idSet, args)
    return Result.success()
  }
}

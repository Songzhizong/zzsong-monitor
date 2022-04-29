package com.zzsong.monitor.center.domain.model.alert

import com.zzsong.monitor.center.args.SelectivityUpdateAlertRuleArgs

/**
 * @author 宋志宗 on 2022/4/25
 */
interface AlertRuleRepository {

  suspend fun save(alertRuleDo: AlertRuleDo): AlertRuleDo

  suspend fun delete(alertRuleDo: AlertRuleDo)

  suspend fun findById(id: Long): AlertRuleDo?

  suspend fun findAllById(ids: Collection<Long>): List<AlertRuleDo>

  suspend fun selectivityUpdate(ids: Set<Long>, args: SelectivityUpdateAlertRuleArgs)
}

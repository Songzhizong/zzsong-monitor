package com.zzsong.monitor.center.application

import cn.idealframework.lang.Sets
import cn.idealframework.transmission.exception.BadRequestException
import cn.idealframework.util.Asserts
import com.zzsong.monitor.center.args.CreateAlertRuleArgs
import com.zzsong.monitor.center.args.SelectivityUpdateAlertRuleArgs
import com.zzsong.monitor.center.domain.model.alert.AlertRuleDo
import com.zzsong.monitor.center.domain.model.alert.AlertRuleRepository
import com.zzsong.monitor.center.domain.model.cluster.ClusterRepository
import com.zzsong.monitor.center.domain.model.staff.BizGroupRepository
import com.zzsong.monitor.center.infrastructure.edge.EdgeConnector
import com.zzsong.monitor.common.constants.AlertRuleType
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author 宋志宗 on 2022/4/25
 */
@Service
@Transactional(rollbackFor = [Throwable::class])
class AlertRuleService(
  private val edgeConnector: EdgeConnector,
  private val clusterRepository: ClusterRepository,
  private val bizGroupRepository: BizGroupRepository,
  private val alertRuleRepository: AlertRuleRepository
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(AlertRuleService::class.java)
  }

  /**
   * 创建告警规则
   *
   * @author 宋志宗 on 2022/4/25
   */
  suspend fun create(args: CreateAlertRuleArgs): AlertRuleDo {
    // 校验业务组是否存在
    val bizGroupId = args.bizGroupId
    if (bizGroupId != null && bizGroupId > 0) {
      bizGroupRepository.findRequiredById(bizGroupId)
    }
    // 校验集群是否存在
    val cluster = args.cluster
    Asserts.notBlank(cluster, "cluster为空");cluster!!
    clusterRepository.findRequiredByCode(cluster)
    val type = args.type
    if (type == AlertRuleType.PROMETHEUS) {
      val prometheus = args.prometheus
      Asserts.nonnull(prometheus, "prometheus配置为空");prometheus!!
      // 校验promql
      val promQl = prometheus.promQl
      val result = edgeConnector.execProm(cluster) {
        it.query(promQl, null).awaitSingle()
      }
      if (result.isFailure) {
        val message = "promQl验证失败: ${result.errorDetails}"
        log.info(message)
        throw BadRequestException(message)
      }
    }
    val alertRuleDo = AlertRuleDo.create(args)
    return alertRuleRepository.save(alertRuleDo)
  }

  suspend fun selectivityUpdate(ids: Set<Long>, args: SelectivityUpdateAlertRuleArgs) {
    if (Sets.isEmpty(ids)) {
      log.info("传入的规则id列表为空")
      return
    }
    val cluster = args.cluster
    if (cluster != null) {
      clusterRepository.findRequiredByCode(cluster)
    }
    val level = args.level
    if (level != null) {
      AlertRuleDo.checkLevel(level)
    }
    val appendTags = args.appendTags
    if (appendTags != null) {
      AlertRuleDo.checkTags(appendTags)
    }
    val enableDaysOfWeek = args.enableDaysOfWeek
    if (enableDaysOfWeek != null) {
      AlertRuleDo.checkEnableDaysOfWeek(enableDaysOfWeek)
    }
    val enableStartTime = args.enableStartTime
    if (enableStartTime != null) {
      AlertRuleDo.checkLocalTime(enableStartTime)
    }
    val enableEndTime = args.enableEndTime
    if (enableEndTime != null) {
      AlertRuleDo.checkLocalTime(enableEndTime)
    }
    alertRuleRepository.selectivityUpdate(ids, args)
  }
}

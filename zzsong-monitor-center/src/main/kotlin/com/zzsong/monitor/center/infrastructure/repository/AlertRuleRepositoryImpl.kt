package com.zzsong.monitor.center.infrastructure.repository

import cn.idealframework.lang.StringUtils
import com.zzsong.monitor.center.args.SelectivityUpdateAlertRuleArgs
import com.zzsong.monitor.center.domain.model.alert.AlertRuleDo
import com.zzsong.monitor.center.domain.model.alert.AlertRuleRepository
import com.zzsong.monitor.center.infrastructure.MonitorIDGenerator
import com.zzsong.monitor.common.constants.NotifyChannel
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

/**
 * @author 宋志宗 on 2022/4/25
 */
@Repository
class AlertRuleRepositoryImpl(
  private val idGenerator: MonitorIDGenerator,
  private val mongoTemplate: ReactiveMongoTemplate,
) : AlertRuleRepository {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(AlertRuleRepositoryImpl::class.java)
  }

  override suspend fun save(alertRuleDo: AlertRuleDo): AlertRuleDo {
    val id = alertRuleDo.id
    if (id < 1) {
      alertRuleDo.id = idGenerator.generate()
      return mongoTemplate.insert(alertRuleDo).awaitSingle()
    }
    return mongoTemplate.save(alertRuleDo).awaitSingle()
  }

  override suspend fun delete(alertRuleDo: AlertRuleDo) {
    mongoTemplate.remove(alertRuleDo).awaitSingleOrNull()
  }

  override suspend fun findById(id: Long): AlertRuleDo? {
    val criteria = Criteria.where("id").`is`(id)
    val query = Query.query(criteria)
    return mongoTemplate.findOne(query, AlertRuleDo::class.java).awaitSingleOrNull()
  }

  override suspend fun findAllById(ids: Collection<Long>): List<AlertRuleDo> {
    val criteria = Criteria.where("id").`in`(ids)
    val query = Query.query(criteria)
    return mongoTemplate.find(query, AlertRuleDo::class.java).collectList().awaitSingle()
  }

  override suspend fun selectivityUpdate(ids: Set<Long>, args: SelectivityUpdateAlertRuleArgs) {
    if (args.isAllNull) {
      log.info("没有任何字段发生变更")
      return
    }
    val criteria = Criteria.where("id").`in`(ids)
    val query = Query.query(criteria)
    val update = Update()
    val cluster = args.cluster
    if (StringUtils.isNotBlank(cluster)) {
      update.set("cluster", cluster)
    }
    val note = args.note
    if (note != null) {
      update.set("note", note)
    }
    val level = args.level
    if (level != null) {
      update.set("level", level)
    }
    val appendTags = args.appendTags
    if (appendTags != null) {
      update.set("appendTags", appendTags)
    }
    val callbackUrls = args.callbackUrls
    if (callbackUrls != null) {
      val filter = callbackUrls
        .filterTo(LinkedHashSet()) { StringUtils.isNotBlank(it) }
      update.set("callbackUrls", filter)
    }
    val notifyChannels = args.notifyChannels
    if (notifyChannels != null) {
      val channels = notifyChannels
        .mapNotNullTo(LinkedHashSet()) { NotifyChannel.ofName(it) }
      update.set("notifyChannels", channels)
    }
    val notifyRepeatStep = args.notifyRepeatStep
    if (notifyRepeatStep != null) {
      update.set("notifyRepeatStep", notifyRepeatStep)
    }
    val recoverDuration = args.recoverDuration
    if (recoverDuration != null) {
      update.set("recoverDuration", recoverDuration)
    }
    val notifyRecovered = args.notifyRecovered
    if (notifyRecovered != null) {
      update.set("notifyRecovered", notifyRecovered)
    }
    val enableDaysOfWeek = args.enableDaysOfWeek
    if (enableDaysOfWeek != null) {
      update.set("enableDaysOfWeek", enableDaysOfWeek)
    }
    val enableStartTime = args.enableStartTime
    if (enableStartTime != null) {
      update.set("enableStartTime", enableStartTime)
    }
    val enableEndTime = args.enableEndTime
    if (enableEndTime != null) {
      update.set("enableEndTime", enableEndTime)
    }
    val enabled = args.enabled
    if (enabled != null) {
      update.set("enabled", enabled)
    }
    update.set("updatedTime", System.currentTimeMillis())
    mongoTemplate.updateMulti(query, update, AlertRuleDo::class.java).awaitSingle()
  }
}

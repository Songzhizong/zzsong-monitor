package com.zzsong.monitor.center.application

import cn.idealframework.transmission.exception.ResourceNotFoundException
import cn.idealframework.util.Asserts
import com.zzsong.monitor.center.args.CreateCollectPlanArgs
import com.zzsong.monitor.center.args.UpdateCollectPlanArgs
import com.zzsong.monitor.center.domain.model.cluster.ClusterRepository
import com.zzsong.monitor.center.domain.model.collect.CollectPlanDo
import com.zzsong.monitor.center.domain.model.collect.CollectPlanRepository
import com.zzsong.monitor.center.domain.model.staff.BizGroupDo
import com.zzsong.monitor.center.domain.model.staff.BizGroupRepository
import com.zzsong.monitor.common.constants.CollectType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * @author 宋志宗 on 2022/4/22
 */
@Service
@Transactional(rollbackFor = [Throwable::class])
class CollectPlanService(
  private val clusterRepository: ClusterRepository,
  private val bizGroupRepository: BizGroupRepository,
  private val collectPlanRepository: CollectPlanRepository,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(CollectPlanService::class.java)
  }

  /**
   * 新建采集计划
   *
   * @author 宋志宗 on 2022/4/22
   */
  suspend fun create(args: CreateCollectPlanArgs): CollectPlanDo {
    val bizGroupId = args.bizGroupId
    val cluster = args.cluster
    val ident = args.ident
    val name = args.name
    val type = args.type
    val prometheus = args.prometheus
    val note = args.note
    var bizGroup: BizGroupDo? = null
    if (bizGroupId != null) {
      bizGroup = bizGroupRepository.findRequiredById(bizGroupId)
    }
    Asserts.notBlank(cluster, "cluster不能为空");cluster!!
    val clusterDo = clusterRepository.findByCode(cluster) ?: kotlin.run {
      log.info("集群: {} 不存在", cluster)
      throw ResourceNotFoundException("集群信息不存在")
    }
    Asserts.notBlank(name, "name不能为空");name!!
    Asserts.nonnull(type, "type不能为空");type!!
    val plan = CollectPlanDo.create(
      bizGroup, clusterDo, ident, name, type, prometheus, note
    )
    val save = collectPlanRepository.save(plan)
    log.info("成功新增采集计划: [{} {}]", save.id, name)
    return save
  }

  suspend fun update(id: Long, args: UpdateCollectPlanArgs): CollectPlanDo {
    val collectPlanDo = collectPlanRepository.findById(id) ?: kotlin.run {
      log.info("采集计划不存在: {}", id)
      throw ResourceNotFoundException("采集计划不存在")
    }
    val name = args.name
    val note = args.note
    val prometheus = args.prometheus
    Asserts.notBlank(name, "name不能为空");name!!
    collectPlanDo.update(name, note, prometheus)
    return collectPlanRepository.save(collectPlanDo)
  }

  suspend fun delete(id: Long) {
    val planDo = collectPlanRepository.findById(id)
    if (planDo == null) {
      log.info("找不到此采集计划: {}", id)
      return
    }
    planDo.delete()
    collectPlanRepository.save(planDo)
    log.info("成功删除采集计划: [{} {}]", id, planDo.name)
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  suspend fun findAllByCluster(cluster: String): List<CollectPlanDo> {
    return collectPlanRepository.findAllByCluster(cluster)
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  suspend fun findAllByClusterAndCollectType(
    cluster: String,
    type: CollectType
  ): List<CollectPlanDo> {
    return collectPlanRepository.findAllByClusterAndCollectType(cluster, type)
  }


  @Transactional(propagation = Propagation.SUPPORTS)
  suspend fun findAllByClusterAndUpdatedTimeGte(
    cluster: String,
    updatedTimeGte: Long
  ): List<CollectPlanDo> {
    return collectPlanRepository.findAllByClusterAndUpdatedTimeGte(cluster, updatedTimeGte)
  }
}

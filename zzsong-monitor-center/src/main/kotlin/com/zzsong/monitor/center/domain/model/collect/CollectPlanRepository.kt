package com.zzsong.monitor.center.domain.model.collect

import com.zzsong.monitor.common.constants.CollectType

/**
 * @author 宋志宗 on 2022/4/22
 */
interface CollectPlanRepository {

  /** 插入或更新 */
  suspend fun save(collectPlanDo: CollectPlanDo): CollectPlanDo

  /** 删除 */
  suspend fun delete(collectPlanDo: CollectPlanDo)

  /** 通过主键查询采集计划 */
  suspend fun findById(id: Long): CollectPlanDo?

  /** 获取某个集群下所有的采集计划 */
  suspend fun findAllByCluster(cluster: String): List<CollectPlanDo>

  /** 获取某个集群下所有的采集计划 */
  suspend fun findAllByClusterAndCollectType(
    cluster: String,
    type: CollectType
  ): List<CollectPlanDo>

  /** 获取某个集群下最近发生变更的采集计划, 包含已被删除的 */
  suspend fun findAllByClusterAndUpdatedTimeGte(
    cluster: String,
    updatedTimeGte: Long
  ): List<CollectPlanDo>
}

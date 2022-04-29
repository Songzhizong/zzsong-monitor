package com.zzsong.monitor.center.infrastructure.repository

import com.zzsong.monitor.center.domain.model.collect.CollectPlanDo
import com.zzsong.monitor.center.domain.model.collect.CollectPlanRepository
import com.zzsong.monitor.center.infrastructure.MonitorIDGenerator
import com.zzsong.monitor.common.constants.CollectType
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * @author 宋志宗 on 2022/4/22
 */
@Repository
class CollectPlanRepositoryImpl(
  private val idGenerator: MonitorIDGenerator,
  private val mongoTemplate: ReactiveMongoTemplate,
) : CollectPlanRepository {

  override suspend fun save(collectPlanDo: CollectPlanDo): CollectPlanDo {
    if (collectPlanDo.id < 1) {
      collectPlanDo.id = idGenerator.generate()
      return mongoTemplate.insert(collectPlanDo).awaitSingle()
    }
    return mongoTemplate.save(collectPlanDo).awaitSingle()
  }

  override suspend fun delete(collectPlanDo: CollectPlanDo) {
    mongoTemplate.remove(collectPlanDo).awaitSingleOrNull()
  }

  override suspend fun findById(id: Long): CollectPlanDo? {
    val criteria = Criteria.where("id").`is`(id)
      .and("deleted").`is`(false)
    val query = Query.query(criteria)
    return mongoTemplate.findOne(query, CollectPlanDo::class.java).awaitSingleOrNull()
  }

  override suspend fun findAllByCluster(cluster: String): List<CollectPlanDo> {
    val criteria = Criteria.where("cluster").`is`(cluster)
      .and("deleted").`is`(false)
    val query = Query.query(criteria)
    return mongoTemplate.find(query, CollectPlanDo::class.java).collectList().awaitSingle()
  }

  override suspend fun findAllByClusterAndCollectType(
    cluster: String,
    type: CollectType
  ): List<CollectPlanDo> {
    val criteria = Criteria.where("cluster").`is`(cluster)
      .and("type").`is`(type)
      .and("deleted").`is`(false)
    val query = Query.query(criteria)
    return mongoTemplate.find(query, CollectPlanDo::class.java).collectList().awaitSingle()
  }

  override suspend fun findAllByClusterAndUpdatedTimeGte(
    cluster: String,
    updatedTimeGte: Long
  ): List<CollectPlanDo> {
    val criteria = Criteria.where("cluster").`is`(cluster)
      .and("updatedTime").gte(updatedTimeGte)
    val query = Query.query(criteria)
    return mongoTemplate.find(query, CollectPlanDo::class.java).collectList().awaitSingle()
  }
}

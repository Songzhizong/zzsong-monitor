package com.zzsong.monitor.center.infrastructure.repository

import com.zzsong.monitor.center.domain.model.resource.ResourceDo
import com.zzsong.monitor.center.domain.model.resource.ResourceRepository
import com.zzsong.monitor.center.infrastructure.MonitorIDGenerator
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * @author 宋志宗 on 2022/3/19
 */
@Repository
class ResourceRepositoryImpl(
  private val idGenerator: MonitorIDGenerator,
  private val mongoTemplate: ReactiveMongoTemplate,
) : ResourceRepository {

  override suspend fun findById(id: Long): ResourceDo? {
    val criteria = Criteria.where("id").`is`(id)
    val query = Query.query(criteria)
    return mongoTemplate.findOne(query, ResourceDo::class.java).awaitSingleOrNull()
  }

  override suspend fun findByClusterAndIdent(cluster: String, ident: String): ResourceDo? {
    val criteria = Criteria.where("cluster").`is`(cluster)
      .and("ident").`is`(ident)
    val query = Query.query(criteria)
    return mongoTemplate.findOne(query, ResourceDo::class.java).awaitSingleOrNull()
  }

  override suspend fun findAllByClusterAndIdentIn(
    cluster: String,
    idents: Collection<String>
  ): List<ResourceDo> {
    val criteria = Criteria.where("cluster").`is`(cluster)
      .and("ident").`in`(idents)
    val query = Query.query(criteria)
    return mongoTemplate.find(query, ResourceDo::class.java)
      .collectList().awaitSingle()
  }
}

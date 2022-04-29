package com.zzsong.monitor.center.infrastructure.repository

import com.zzsong.monitor.center.domain.model.cluster.ClusterDo
import com.zzsong.monitor.center.domain.model.cluster.ClusterRepository
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
class ClusterRepositoryImpl(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val idGenerator: MonitorIDGenerator,
) : ClusterRepository {

  override suspend fun save(clusterDo: ClusterDo): ClusterDo {
    val id = clusterDo.id
    if (id == null || id < 1) {
      clusterDo.id = idGenerator.generate()
      return mongoTemplate.insert(clusterDo).awaitSingle()
    }
    return mongoTemplate.save(clusterDo).awaitSingle()
  }

  override suspend fun delete(clusterDo: ClusterDo) {
    mongoTemplate.remove(clusterDo).awaitSingleOrNull()
  }

  override suspend fun findById(id: Long): ClusterDo? {
    val criteria = Criteria.where("id").`is`(id)
    val query = Query.query(criteria)
    return mongoTemplate.findOne(query, ClusterDo::class.java).awaitSingleOrNull()
  }

  override suspend fun findByCode(code: String): ClusterDo? {
    val criteria = Criteria.where("code").`is`(code)
    val query = Query.query(criteria)
    return mongoTemplate.findOne(query, ClusterDo::class.java).awaitSingleOrNull()
  }

  override suspend fun findAll(): List<ClusterDo> {
    return mongoTemplate.findAll(ClusterDo::class.java).collectList().awaitSingle()
  }
}

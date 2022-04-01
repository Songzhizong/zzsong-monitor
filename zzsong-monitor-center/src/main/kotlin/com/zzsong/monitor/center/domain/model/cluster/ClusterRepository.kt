package com.zzsong.monitor.center.domain.model.cluster

import cn.idealframework.transmission.exception.ResourceNotFoundException
import com.zzsong.monitor.center.infrastructure.MonitorIDGenerator
import com.zzsong.monitor.center.infrastructure.mongo.MongoClusterRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Repository

/**
 * @author 宋志宗 on 2022/3/19
 */
@Repository
class ClusterRepository(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val idGenerator: MonitorIDGenerator,
  private val clusterRepository: MongoClusterRepository
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(ClusterRepository::class.java)
  }

  suspend fun save(clusterDo: ClusterDo): ClusterDo {
    val id = clusterDo.id
    if (id == null || id < 1) {
      clusterDo.id = idGenerator.generate()
      return mongoTemplate.insert(clusterDo).awaitSingle()
    }
    return mongoTemplate.save(clusterDo).awaitSingle()
  }

  suspend fun delete(clusterDo: ClusterDo) {
    clusterRepository.delete(clusterDo).awaitSingleOrNull()
  }

  suspend fun findById(id: Long): ClusterDo? {
    return clusterRepository.findById(id).awaitSingleOrNull()
  }

  suspend fun findRequiredById(id: Long): ClusterDo {
    return findById(id) ?: kotlin.run {
      log.info("集群: {} 不存在", id)
      throw ResourceNotFoundException("集群不存在")
    }
  }

  suspend fun findByCode(code: String): ClusterDo? {
    return clusterRepository.findByCode(code).awaitSingleOrNull()
  }
}

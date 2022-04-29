package com.zzsong.monitor.center.domain.model.cluster

import cn.idealframework.transmission.exception.ResourceNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author 宋志宗 on 2022/3/19
 */
interface ClusterRepository {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(ClusterRepository::class.java)
  }

  suspend fun save(clusterDo: ClusterDo): ClusterDo

  suspend fun delete(clusterDo: ClusterDo)

  suspend fun findById(id: Long): ClusterDo?

  suspend fun findRequiredById(id: Long): ClusterDo {
    return findById(id) ?: kotlin.run {
      log.info("集群: {} 不存在", id)
      throw ResourceNotFoundException("集群不存在")
    }
  }

  suspend fun findByCode(code: String): ClusterDo?

  suspend fun findRequiredByCode(code: String): ClusterDo {
    return findByCode(code) ?: kotlin.run {
      log.info("集群: {} 不存在", code)
      throw ResourceNotFoundException("集群不存在")
    }
  }

  suspend fun findAll(): List<ClusterDo>

}

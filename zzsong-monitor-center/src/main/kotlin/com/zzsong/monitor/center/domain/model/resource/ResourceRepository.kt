package com.zzsong.monitor.center.domain.model.resource

import cn.idealframework.lang.CollectionUtils
import com.zzsong.monitor.center.infrastructure.MonitorIDGenerator
import com.zzsong.monitor.center.infrastructure.mongo.MongoResourceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository

/**
 * @author 宋志宗 on 2022/3/19
 */
@Repository
class ResourceRepository(
  private val resourceCache: ResourceCache,
  private val mongoTemplate: ReactiveMongoTemplate,
  private val idGenerator: MonitorIDGenerator,
  private val resourceRepository: MongoResourceRepository,
) {

  suspend fun save(resourceDo: ResourceDo): ResourceDo {
    resourceCache.invalidate(resourceDo.cluster, resourceDo.ident)
    val result = if (resourceDo.id < 1) {
      resourceDo.id = idGenerator.generate()
      mongoTemplate.insert(resourceDo).awaitSingle()
    } else {
      mongoTemplate.save(resourceDo).awaitSingle()
    }
    resourceCache.put(result)
    return result
  }

  suspend fun saveAll(resources: List<ResourceDo>): List<ResourceDo> {
    if (CollectionUtils.isEmpty(resources)) {
      return emptyList()
    }
    val inserts = ArrayList<ResourceDo>()
    val updates = ArrayList<ResourceDo>()
    for (resource in resources) {
      if (resource.id < 1) {
        resource.id = idGenerator.generate()
        inserts.add(resource)
      } else {
        updates.add(resource)
      }
    }
    // 先删除缓存, 防止出现脏数据
    resourceCache.invalidateAll(resources)
    return coroutineScope {
      val insertAsync = async {
        if (inserts.isNotEmpty()) {
          mongoTemplate.insertAll(inserts).collectList().awaitSingle()
        } else {
          emptyList()
        }
      }
      val updateAsync = async {
        if (updates.isNotEmpty()) {
          resourceRepository.saveAll(updates).collectList().awaitSingle()
        } else {
          emptyList()
        }
      }
      val result = buildList { addAll(insertAsync.await());addAll(updateAsync.await()) }
      resourceCache.putAll(result)
      result
    }
  }

  suspend fun delete(resourceDo: ResourceDo) {
    //  删两次缓存, 防止缓存中出现脏数据
    resourceCache.invalidate(resourceDo.cluster, resourceDo.ident)
    resourceRepository.delete(resourceDo).awaitSingleOrNull()
    resourceCache.invalidate(resourceDo.cluster, resourceDo.ident)
  }

  suspend fun deleteAllByClusterAndIdentIn(
    cluster: String,
    idents: Collection<String>
  ): List<ResourceDo> {
    val criteria = Criteria
      .where("cluster").isEqualTo(cluster)
      .and("ident").inValues(idents)
    val query = Query.query(criteria)
    return mongoTemplate.findAllAndRemove(query, ResourceDo::class.java).collectList().awaitSingle()
  }

  suspend fun findById(id: Long): ResourceDo? {
    return resourceRepository.findById(id).awaitSingleOrNull()
  }

  suspend fun findByClusterAndIdent(cluster: String, ident: String): ResourceDo? {
    return resourceRepository.findByClusterAndIdent(cluster, ident).awaitSingleOrNull()
  }

  suspend fun findAllByClusterAndIdentIn(
    cluster: String,
    idents: Collection<String>
  ): List<ResourceDo> {
    return resourceRepository.findAllByClusterAndIdentIn(cluster, idents).collectList()
      .awaitSingle()
  }
}

package com.zzsong.monitor.center.infrastructure.repository

import cn.idealframework.lang.CollectionUtils
import com.zzsong.monitor.center.domain.model.resource.ModifiableResourceRepository
import com.zzsong.monitor.center.domain.model.resource.ResourceCache
import com.zzsong.monitor.center.domain.model.resource.ResourceDo
import com.zzsong.monitor.center.infrastructure.MonitorIDGenerator
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
import reactor.core.publisher.Flux

/**
 * @author 宋志宗 on 2022/4/21
 */
@Repository
class ModifiableResourceRepositoryImpl(
  private val resourceCache: ResourceCache,
  private val mongoTemplate: ReactiveMongoTemplate,
  private val idGenerator: MonitorIDGenerator,
) : ModifiableResourceRepository {

  override suspend fun save(resourceDo: ResourceDo): ResourceDo {
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

  override suspend fun saveAll(resources: List<ResourceDo>): List<ResourceDo> {
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
    resourceCache.invalidateAll(updates)
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
          Flux.fromIterable(updates).flatMap { mongoTemplate.save(it) }.collectList().awaitSingle()
        } else {
          emptyList()
        }
      }
      val result = buildList {
        addAll(insertAsync.await())
        addAll(updateAsync.await())
      }
      resourceCache.putAll(result)
      result
    }
  }

  override suspend fun delete(resourceDo: ResourceDo) {
    mongoTemplate.remove(resourceDo).awaitSingleOrNull()
    resourceCache.invalidate(resourceDo.cluster, resourceDo.ident)
  }

  override suspend fun deleteAllByClusterAndIdentIn(
    cluster: String,
    idents: Collection<String>
  ): List<ResourceDo> {
    val criteria = Criteria
      .where("cluster").isEqualTo(cluster)
      .and("ident").inValues(idents)
    val query = Query.query(criteria)
    return mongoTemplate.findAllAndRemove(query, ResourceDo::class.java).collectList().awaitSingle()
  }
}

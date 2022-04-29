package com.zzsong.monitor.center.domain.model.resource

/**
 * @author 宋志宗 on 2022/4/21
 */
interface ModifiableResourceRepository {

  suspend fun save(resourceDo: ResourceDo): ResourceDo

  suspend fun saveAll(resources: List<ResourceDo>): List<ResourceDo>

  suspend fun delete(resourceDo: ResourceDo)

  suspend fun deleteAllByClusterAndIdentIn(
    cluster: String,
    idents: Collection<String>
  ): List<ResourceDo>
}

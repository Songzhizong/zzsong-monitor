package com.zzsong.monitor.center.domain.model.resource

/**
 * @author 宋志宗 on 2022/3/19
 */
interface ResourceRepository {

  suspend fun findById(id: Long): ResourceDo?

  suspend fun findByClusterAndIdent(cluster: String, ident: String): ResourceDo?

  suspend fun findAllByClusterAndIdentIn(
    cluster: String,
    idents: Collection<String>
  ): List<ResourceDo>
}

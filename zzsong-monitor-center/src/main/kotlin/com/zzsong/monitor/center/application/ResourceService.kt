package com.zzsong.monitor.center.application

import com.zzsong.monitor.center.domain.model.resource.ModifiableResourceRepository
import com.zzsong.monitor.center.domain.model.resource.ResourceCache
import com.zzsong.monitor.center.domain.model.resource.ResourceDo
import com.zzsong.monitor.center.domain.model.resource.ResourceRepository
import com.zzsong.monitor.center.domain.model.staff.BizGroupDo
import com.zzsong.monitor.center.domain.model.staff.BizGroupRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author 宋志宗 on 2022/3/19
 */
@Service
@Transactional(rollbackFor = [Throwable::class])
class ResourceService(
  private val resourceCache: ResourceCache,
  private val resourceRepository: ResourceRepository,
  private val bizGroupRepository: BizGroupRepository,
  private val modifiableResourceRepository: ModifiableResourceRepository,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(ResourceService::class.java)
  }

  /**
   * 如果资源不存在则创建并返回新创建的资源列表
   *
   * @author 宋志宗 on 2022/3/26
   */
  suspend fun createIfAbsent(cluster: String, idents: Collection<String>): List<ResourceDo> {
    if (idents.isEmpty()) {
      return emptyList()
    }
    // 先走一遍缓存, 将已存在的资源过滤掉
    val filter = idents
      .filterTo(HashSet()) { resourceCache.getIfPresent(cluster, it) == null }
    if (filter.isEmpty()) {
      return emptyList()
    }
    // 将过滤出来的ident从数据库中查一下, 防止有不在缓存中的资源信息
    val existIdents = resourceRepository
      .findAllByClusterAndIdentIn(cluster, filter)
      .mapTo(HashSet<String>()) { it.ident }
    if (existIdents.size == filter.size) {
      return emptyList()
    }
    // 不存在的就新增
    filter.removeAll(existIdents)
    val resourceDos = filter.map { ResourceDo.create(cluster, it) }
    return try {
      modifiableResourceRepository.saveAll(resourceDos)
    } catch (e: DuplicateKeyException) {
      emptyList()
    }
  }

  /**
   * 批量修改业务组
   *
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun changeGroup(cluster: String, idents: Collection<String>, bizGroupId: Long?) {
    var bizGroup: BizGroupDo? = null
    if (bizGroupId != null && bizGroupId > 1) {
      bizGroup = bizGroupRepository.findRequiredById(bizGroupId)
    }
    val resourceDos = resourceRepository.findAllByClusterAndIdentIn(cluster, idents)
      .onEach { it.changeGroup(bizGroup) }
    modifiableResourceRepository.saveAll(resourceDos)
  }

  /**
   * 批量添加标签
   *
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun addTags(cluster: String, idents: Collection<String>, tags: Collection<String>?) {
    val resourceDos = resourceRepository.findAllByClusterAndIdentIn(cluster, idents)
      .onEach { it.addTags(tags) }
    modifiableResourceRepository.saveAll(resourceDos)
  }

  /**
   * 批量移除标签
   *
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun removeTags(cluster: String, idents: Collection<String>, tags: Collection<String>?) {
    val resourceDos = resourceRepository.findAllByClusterAndIdentIn(cluster, idents)
      .onEach { it.removeTags(tags) }
    modifiableResourceRepository.saveAll(resourceDos)
  }

  /**
   * 批量变更备注
   *
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun changeNote(cluster: String, idents: Collection<String>, note: String?) {
    val resourceDos = resourceRepository.findAllByClusterAndIdentIn(cluster, idents)
      .onEach { it.changeNote(note) }
    modifiableResourceRepository.saveAll(resourceDos)
  }

  /**
   * 批量删除监控目标
   *
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun delete(cluster: String, idents: Collection<String>) {
    val deleted =
      modifiableResourceRepository.deleteAllByClusterAndIdentIn(cluster, idents)
    val count = deleted.size
    if (count > 0) {
      log.info("成功删除监控目标 {}条", count)
    }
  }
}

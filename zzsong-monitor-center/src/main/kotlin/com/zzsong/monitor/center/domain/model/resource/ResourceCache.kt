package com.zzsong.monitor.center.domain.model.resource

import cn.idealframework.json.JsonUtils
import com.zzsong.monitor.center.configure.MonitorCenterProperties
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * 资源信息缓存
 *
 * @author 宋志宗 on 2022/3/25
 */
@Component
class ResourceCache(
  properties: MonitorCenterProperties,
  private val redisTemplate: ReactiveStringRedisTemplate
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(ResourceCache::class.java)
    private val timeout = Duration.ofHours(48)
  }

  private val prefix: String

  init {
    val cachePrefix = properties.cachePrefix
    this.prefix = if (cachePrefix == null || cachePrefix.isBlank()) {
      ""
    } else {
      "$cachePrefix:"
    }
  }

  fun put(resource: ResourceDo) {
    val cluster = resource.cluster
    val ident = resource.ident
    val redisKey = buildKey(cluster, ident)
    val value = JsonUtils.toJsonString(resource)
    redisTemplate.opsForValue().set(redisKey, value, timeout).subscribe()
  }

  fun putAll(resources: List<ResourceDo>) {
    if (resources.isEmpty()) {
      return
    }
    for (resource in resources) {
      put(resource)
    }
  }

  suspend fun getIfPresent(cluster: String, ident: String): ResourceDo? {
    val redisKey = buildKey(cluster, ident)
    val value = redisTemplate.opsForValue().get(redisKey).awaitSingleOrNull()
    if (value == null || value.isBlank()) {
      return null
    }
    return JsonUtils.parse(value, ResourceDo::class.java)
  }

  suspend fun invalidate(cluster: String, ident: String) {
    val redisKey = buildKey(cluster, ident)
    redisTemplate.delete(redisKey).awaitSingleOrNull()
  }

  suspend fun invalidateAll(resources: List<ResourceDo>) {
    for (resource in resources) {
      invalidate(resource.cluster, resource.ident)
    }
  }

  private fun buildKey(cluster: String, ident: String): String {
    return prefix + "resource:" + cluster + ":" + ident
  }
}

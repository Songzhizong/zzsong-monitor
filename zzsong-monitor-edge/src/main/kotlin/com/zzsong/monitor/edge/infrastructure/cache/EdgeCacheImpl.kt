package com.zzsong.monitor.edge.infrastructure.cache

import com.zzsong.monitor.edge.infrastructure.center.CenterClient
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.stereotype.Component

/**
 * 缓存实现
 *
 * @author 宋志宗 on 2022/3/19
 */
@Component
class EdgeCacheImpl(
  private val centerClient: CenterClient
) : EdgeCache, SmartInitializingSingleton {
  /** 资源自定义标签缓存, 如果没有自定义标签则value应该是一个空的map */
  private var tagCache = emptyMap<String, Map<String, String>>()

  override fun getTags(ident: String): Map<String, String> {
    return tagCache[ident] ?: emptyMap()
  }

  override fun possibleNewResource(ident: String): Boolean {
    return tagCache[ident] == null
  }

  override fun afterSingletonsInstantiated() {

  }
}

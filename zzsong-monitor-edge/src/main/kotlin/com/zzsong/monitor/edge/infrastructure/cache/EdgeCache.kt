package com.zzsong.monitor.edge.infrastructure.cache

/**
 * 边缘节点缓存
 *
 * @author 宋志宗 on 2022/3/20
 */
interface EdgeCache {

  /** 获取指定对象的附加标签 */
  fun getTags(ident: String): Map<String, String>

  /** 判断资源是否可能是新发现的资源 */
  fun possibleNewResource(ident: String): Boolean
}

package com.zzsong.monitor.edge.infrastructure.client

import com.zzsong.monitor.common.constants.CollectType
import com.zzsong.monitor.common.pojo.CollectPlan

/**
 * 中心节点客户端
 *
 * @author 宋志宗 on 2022/3/19
 */
interface CenterClient {

  /** 资源发现通知 */
  suspend fun resourceDiscovered(cluster: String, idents: Set<String>)

  /**
   * 获取指定集群下所有的采集计划
   *
   * @param cluster 集群编码
   * @author 宋志宗 on 2022/4/22
   */
  suspend fun findCollectPlan(cluster: String): List<CollectPlan>

  /**
   * 获取指定集群下某个采集类型所有的的采集计划
   *
   * @param cluster 集群编码
   * @param type    采集类型
   * @author 宋志宗 on 2022/4/22
   */
  suspend fun findCollectPlan(cluster: String, type: CollectType): List<CollectPlan>

  /**
   * 获取指定集群下最近发生变更的采集计划
   *
   * @param cluster 集群编码
   * @param updatedTimeGte 时间戳,此时间之后发生变更的数据 必填
   * @author 宋志宗 on 2022/4/22
   */
  suspend fun findRecentlyModifiedCollectPlan(
    cluster: String,
    updatedTimeGte: Long
  ): List<CollectPlan>
}

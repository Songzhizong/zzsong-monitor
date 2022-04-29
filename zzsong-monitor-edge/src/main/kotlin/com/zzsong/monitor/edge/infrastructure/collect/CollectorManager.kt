package com.zzsong.monitor.edge.infrastructure.collect

import com.zzsong.monitor.common.constants.CollectType
import com.zzsong.monitor.common.pojo.CollectPlan
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * 采集器管理器
 *
 * @author 宋志宗 on 2022/4/24
 */
interface CollectorManager {

  /** 管理的采集器类型 */
  fun type(): CollectType

  @DelicateCoroutinesApi
  suspend fun refresh(all: List<CollectPlan>)

  /** 采集计划信息发生了变更 */
  @DelicateCoroutinesApi
  suspend fun update(update: List<CollectPlan>)
}

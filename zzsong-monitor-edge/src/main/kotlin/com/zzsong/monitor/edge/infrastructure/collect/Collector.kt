package com.zzsong.monitor.edge.infrastructure.collect

import com.zzsong.monitor.common.constants.CollectType
import com.zzsong.monitor.common.pojo.CollectPlan
import com.zzsong.monitor.common.pojo.TimeSeries

/**
 * @author 宋志宗 on 2022/4/22
 */
interface Collector {

  fun type(): CollectType

  suspend fun collect(collectPlan: CollectPlan): List<TimeSeries>
}

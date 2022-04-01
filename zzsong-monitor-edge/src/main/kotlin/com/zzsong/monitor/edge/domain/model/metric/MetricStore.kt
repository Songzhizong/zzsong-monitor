package com.zzsong.monitor.edge.domain.model.metric

import com.zzsong.monitor.common.pojo.Metric
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * 指标数据存储库
 *
 * @author 宋志宗 on 2022/3/18
 */
interface MetricStore {

  @DelicateCoroutinesApi
  suspend fun write(metrics: List<Metric>)
}

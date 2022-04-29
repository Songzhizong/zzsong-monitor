package com.zzsong.monitor.edge.application

import com.zzsong.monitor.common.pojo.TimeSeries
import com.zzsong.monitor.edge.infrastructure.output.OutputExchange
import kotlinx.coroutines.DelicateCoroutinesApi
import org.springframework.stereotype.Service

/**
 * @author 宋志宗 on 2022/4/21
 */
@Service
class TimeSeriesService(
  private val outputExchange: OutputExchange
) {

  @DelicateCoroutinesApi
  suspend fun saveAll(timeSeriesList: List<TimeSeries>) {
    if (timeSeriesList.isEmpty()) {
      return
    }
    val processed = timeSeriesList.filter {
      true
    }
    if (processed.isEmpty()) {
      return
    }
    // 写出指标数据
    outputExchange.output(processed)
  }
}

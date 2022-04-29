package com.zzsong.monitor.edge.infrastructure.output

import com.zzsong.monitor.common.pojo.TimeSeries

/**
 * 指标数据写出通道
 *
 * @author 宋志宗 on 2022/4/22
 */
interface OutputChannel {

  fun ready(): Boolean

  suspend fun output(timeSeriesList: List<TimeSeries>)
}

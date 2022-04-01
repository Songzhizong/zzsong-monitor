package com.zzsong.monitor.edge.infrastructure.prometheus

import com.zzsong.monitor.common.pojo.TimeSeries
import com.zzsong.monitor.common.prometheus.PrometheusMetadata
import com.zzsong.monitor.common.prometheus.PrometheusQueryRangeResp
import com.zzsong.monitor.common.prometheus.PrometheusQueryResp
import com.zzsong.monitor.common.prometheus.PrometheusResult

/**
 * @author 宋志宗 on 2022/3/18
 */
interface PrometheusClient {

  /**
   * 远程写入时序数据
   *
   * @param url 支持remote-write协议的完整写入地址
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun remoteWrite(url: String, timeSeries: Collection<TimeSeries>)

  /**
   * 获取元数据
   *
   * @param baseUrl prometheus的基础访问地址
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun metadata(baseUrl: String): PrometheusResult<Map<String, List<PrometheusMetadata>>>

  /**
   * 获取所有标签
   *
   * @param baseUrl prometheus的基础访问地址
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun getLabels(baseUrl: String): PrometheusResult<List<String>>

  /**
   * 获取指定标签在一一定时间区间内所有的值
   *
   * @param baseUrl prometheus的基础访问地址
   * @param label   标签名称
   * @param start   起始时间 2022-03-18T07:11:32.560Z
   * @param end     结束时间 2022-03-18T19:11:32.560Z
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun labelValues(
    baseUrl: String,
    label: String,
    start: String,
    end: String
  ): PrometheusResult<List<String>>


  /**
   * 指定时间查询指标数据
   *
   * @param baseUrl prometheus的基础访问地址
   * @param query  promQL
   * @param time   妙计时间戳 1647668067.734
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun query(
    baseUrl: String,
    query: String,
    time: Double?
  ): PrometheusResult<PrometheusQueryResp>

  /**
   * 按时间区间查询数据
   *
   * @param baseUrl prometheus的基础访问地址
   * @param query   promQL
   * @param start   起始时间戳  浮点型 秒级
   * @param end     结束时间戳  浮点型 秒级
   * @param step    步长,单位秒
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun queryRange(
    baseUrl: String,
    query: String,
    start: Double,
    end: Double,
    step: Int
  ): PrometheusResult<PrometheusQueryRangeResp>

  suspend fun series(
    baseUrl: String,
    start: String?,
    end: String?,
    match: String,
  ): PrometheusResult<List<Map<String, String>>>
}

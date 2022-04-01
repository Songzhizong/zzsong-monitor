package com.zzsong.monitor.edge.port.http

import cn.idealframework.util.Asserts
import com.zzsong.monitor.common.prometheus.PrometheusMetadata
import com.zzsong.monitor.common.prometheus.PrometheusQueryRangeResp
import com.zzsong.monitor.common.prometheus.PrometheusResult
import com.zzsong.monitor.common.prometheus.exception.PrometheusException
import com.zzsong.monitor.edge.application.PrometheusService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * prometheus查询接口
 *
 * @author 宋志宗 on 2022/3/19
 */
@RestController
@RequestMapping("/prometheus")
class PrometheusController(private val prometheusService: PrometheusService) {

  @GetMapping("/api/v1/labels")
  suspend fun getLabels(): ResponseEntity<PrometheusResult<List<String>>> {
    return try {
      success(prometheusService.allLabels())
    } catch (e: Exception) {
      handleException(e)
    }
  }

  /**
   * 获取元数据
   *
   * http://127.0.0.1:9090/api/v1/metadata
   */
  @GetMapping("/api/v1/metadata")
  suspend fun metadata(): PrometheusResult<Map<String, List<PrometheusMetadata>>> {
    return PrometheusResult.data(null)
  }

  /**
   * 获取一定时间内指定label的全部value
   *
   * @param label 标签名称
   * @param start 起始时间 2022-03-18T07:11:32.560Z
   * @param end   结束时间 2022-03-18T19:11:32.560Z
   * http://127.0.0.1:9090/api/v1/label/ident/values?start=2022-03-18T07:11:32.560Z&end=2022-03-18T19:11:32.560Z
   */
  @GetMapping("/api/v1/label/{label}/values")
  suspend fun labelValues(
    @PathVariable label: String,
    start: String?,
    end: String?,
  ): PrometheusResult<List<String>> {
    return PrometheusResult.data(emptyList())
  }

  /**
   * 指定时间查询指标数据
   *
   * http://127.0.0.1:9090/api/v1/query?query=cpu_usage_idle{cpu="cpu-total"}&time=1647630052.249
   */
  @GetMapping("/api/v1/query")
  suspend fun query(query: String?, time: Double?): PrometheusResult<PrometheusQueryRangeResp> {
    return PrometheusResult.data(null)
  }

  /**
   * 按时间区间查询数据
   *
   * @param query promQL
   * @param start 起始时间戳  浮点型 秒级
   * @param end   结束时间戳  浮点型 秒级
   * @param step  步长,单位秒
   * http://127.0.0.1:9090/api/v1/query_range?query=cpu_usage_idle{cpu="cpu-total"}&start=1647622826.77&end=1647630026.77&step=1
   */
  @GetMapping("/api/v1/query_range")
  suspend fun queryRange(
    query: String?,
    start: Double?,
    end: Double?,
    step: Int?
  ): ResponseEntity<PrometheusResult<PrometheusQueryRangeResp>> {
    return try {
      Asserts.notBlank(query, "query is blank");query!!
      Asserts.nonnull(start, "start timestamp is null");start!!
      Asserts.nonnull(end, "end timestamp is null");end!!
      Asserts.nonnull(step, "step is null");step!!
      success(PrometheusResult.data(null))
    } catch (e: Exception) {
      handleException(e)
    }
  }

  /**
   * http://127.0.0.1:9090/api/v1/query_exemplars?query=cpu_usage_idle{cpu="cpu-total"}&start=1647626778.518&end=1647630378.518
   */
  @GetMapping("/api/v1/query_exemplars")
  suspend fun queryExemplars(): PrometheusResult<PrometheusQueryRangeResp> {
    return PrometheusResult.data(null)
  }

  fun <T> success(result: PrometheusResult<T>): ResponseEntity<PrometheusResult<T>> {
    return ResponseEntity<PrometheusResult<T>>(result, HttpStatus.OK)
  }

  private fun <T> handleException(e: Exception): ResponseEntity<PrometheusResult<T>> {
    if (e is PrometheusException) {
      val httpStatus = e.httpStatus
      val errorType = e.errorType ?: "bad_data"
      val error = e.error ?: e.message ?: "unknown"
      val prometheusResult = PrometheusResult.error<T>(errorType, error)
      return ResponseEntity<PrometheusResult<T>>(prometheusResult, HttpStatus.valueOf(httpStatus))
    }
    val errorType = "internal_server_error"
    val error = e.message ?: e.javaClass.name
    val prometheusResult = PrometheusResult.error<T>(errorType, error)
    return ResponseEntity<PrometheusResult<T>>(prometheusResult, HttpStatus.INTERNAL_SERVER_ERROR)
  }
}

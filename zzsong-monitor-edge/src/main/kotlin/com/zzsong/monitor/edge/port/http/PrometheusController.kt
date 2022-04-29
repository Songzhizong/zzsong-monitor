package com.zzsong.monitor.edge.port.http

import cn.idealframework.json.JsonUtils
import cn.idealframework.util.Asserts
import com.zzsong.monitor.edge.application.PrometheusService
import com.zzsong.monitor.edge.application.TimeSeriesService
import com.zzsong.monitor.edge.infrastructure.utils.HttpUtils
import com.zzsong.monitor.prometheus.data.PrometheusResult
import com.zzsong.monitor.prometheus.data.QueryResp
import com.zzsong.monitor.prometheus.protobuf.Remote
import com.zzsong.monitor.prometheus.util.PrometheusProtoUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.server.awaitFormData
import org.springframework.web.server.ServerWebExchange

/**
 * prometheus接口
 *
 * @author 宋志宗 on 2022/4/25
 */
@RestController
@RequestMapping("/edge/monitor/prometheus")
class PrometheusController(
  private val prometheusService: PrometheusService,
  private val timeSeriesService: TimeSeriesService
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(PrometheusController::class.java)
  }

  /**
   * prometheus remote write协议写入数据
   *
   * @author 宋志宗 on 2022/4/22
   */
  @DelicateCoroutinesApi
  @PostMapping("/write")
  suspend fun prometheusWrite(
    @RequestHeader(required = false, name = "Content-Encoding")
    contentEncoding: String?,
    @RequestBody(required = false)
    body: ByteArray
  ) {
    GlobalScope.launch(Dispatchers.Default) {
      try {
        val bytes = HttpUtils.uncompressBody(body, contentEncoding) ?: return@launch
        val writeRequest = Remote.WriteRequest.parseFrom(bytes)
        val timeSeriesList = PrometheusProtoUtils.parseWriteRequest(writeRequest)
        if (log.isDebugEnabled) {
          log.debug("接收到prometheus remote write数据: {}", JsonUtils.toJsonString(timeSeriesList))
        }
        timeSeriesService.saveAll(timeSeriesList)
      } catch (e: Exception) {
        log.info("保存prometheus remote write数据发生异常 {}: ", e.message)
      }
    }
  }

  @GetMapping("/api/v1/metadata")
  suspend fun metadata(): PrometheusResult<Map<String, List<com.zzsong.monitor.prometheus.data.Metadata>>> {
    return try {
      prometheusService.metadata()
    } catch (e: Asserts.AssertException) {
      PrometheusResult.error("bad_data", e.message)
    } catch (e: Exception) {
      log.info("queryRange exception: {} {}", e.javaClass.name, e.message)
      PrometheusResult.exception(e)
    }
  }

  @GetMapping("/api/v1/labels")
  suspend fun getLabels(
    @RequestParam("match[]")
    match: String?,
    start: Double?,
    end: Double?
  ): PrometheusResult<List<String>> {
    return try {
      prometheusService.getLabels(match, start, end)
    } catch (e: Asserts.AssertException) {
      PrometheusResult.error("bad_data", e.message)
    } catch (e: Exception) {
      log.info("queryRange exception: {} {}", e.javaClass.name, e.message)
      PrometheusResult.exception(e)
    }
  }

  @GetMapping("/api/v1/label/{label}/values")
  suspend fun labelValues(
    @PathVariable("label")
    label: String,
    start: Double?,
    end: Double?,
    match: String?
  ): PrometheusResult<List<String>> {
    return try {
      prometheusService.labelValues(label, start, end, match)
    } catch (e: Asserts.AssertException) {
      PrometheusResult.error("bad_data", e.message)
    } catch (e: Exception) {
      log.info("queryRange exception: {} {}", e.javaClass.name, e.message)
      PrometheusResult.exception(e)
    }
  }

  @PostMapping("/api/v1/series")
  suspend fun series(
    @RequestParam("match[]")
    match: String?,
    start: String?,
    end: String?,
    serverWebExchange: ServerWebExchange
  ): PrometheusResult<List<Map<String, String>>> {
    return try {
      val formData = serverWebExchange.awaitFormData()
      val match1 = formData.getFirst("match[]") ?: match
      val startStr = formData.getFirst("start") ?: start
      val endStr = formData.getFirst("end") ?: end
      Asserts.nonnull(
        match1,
        "invalid parameter \\\"match[]\\\": 1:1: parse error: unexpected end of input"
      );match1!!
      prometheusService.series(match1, startStr, endStr)
    } catch (e: Asserts.AssertException) {
      PrometheusResult.error("bad_data", e.message)
    } catch (e: Exception) {
      log.info("queryRange exception: {} {}", e.javaClass.name, e.message)
      PrometheusResult.exception(e)
    }
  }

  @GetMapping("/api/v1/query")
  suspend fun query(query: String?, time: Double?): PrometheusResult<QueryResp> {
    return try {
      Asserts.nonnull(
        query,
        "invalid parameter \"query\": 1:1: parse error: no expression found in input"
      );query!!
      prometheusService.query(query, time)
    } catch (e: Asserts.AssertException) {
      PrometheusResult.error("bad_data", e.message)
    } catch (e: Exception) {
      log.info("queryRange exception: {} {}", e.javaClass.name, e.message)
      PrometheusResult.exception(e)
    }
  }

  @GetMapping("/api/v1/query_range")
  suspend fun queryRange(
    query: String?,
    start: Double?,
    end: Double?,
    step: Int?
  ): PrometheusResult<QueryResp> {
    return try {
      Asserts.nonnull(query, "1:1: parse error: no expression found in input");query!!
      Asserts.nonnull(
        start,
        "invalid parameter \"start\": cannot parse \"\" to a valid timestamp"
      );start!!
      Asserts.nonnull(
        end,
        "invalid parameter \"end\": cannot parse \"\" to a valid timestamp"
      );end!!
      Asserts.nonnull(
        step,
        "invalid parameter \"step\": cannot parse \"\" to a valid duration"
      );step!!
      prometheusService.queryRange(query, start, end, step)
    } catch (e: Asserts.AssertException) {
      PrometheusResult.error("bad_data", e.message)
    } catch (e: Exception) {
      log.info("queryRange exception: {} {}", e.javaClass.name, e.message)
      PrometheusResult.exception(e)
    }
  }
}

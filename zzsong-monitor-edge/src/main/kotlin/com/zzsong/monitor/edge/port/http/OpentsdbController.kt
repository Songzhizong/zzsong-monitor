package com.zzsong.monitor.edge.port.http

import cn.idealframework.json.JsonUtils
import com.zzsong.monitor.common.pojo.Metric
import com.zzsong.monitor.edge.application.TimeSeriesService
import com.zzsong.monitor.edge.infrastructure.utils.HttpUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

/**
 * opentsdb接口
 *
 * @author 宋志宗 on 2022/4/25
 */
@RestController
@RequestMapping("/edge/monitor/opentsdb")
class OpentsdbController(private val timeSeriesService: TimeSeriesService) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(OpentsdbController::class.java)
  }

  /**
   * 数据写入
   *
   * @author 宋志宗 on 2022/4/21
   */
  @DelicateCoroutinesApi
  @PostMapping("/put")
  suspend fun opentsdbPut(
    @RequestHeader(required = false, name = "Content-Encoding")
    contentEncoding: String?,
    @RequestBody(required = false)
    body: ByteArray
  ) {
    GlobalScope.launch(Dispatchers.Default) {
      try {
        val bytes = HttpUtils.uncompressBody(body, contentEncoding) ?: return@launch
        val bodyStr = String(bytes, Charsets.UTF_8)
        val metrics = when (bodyStr.first()) {
          '[' -> {
            JsonUtils.parseList(bodyStr, Metric::class.java)
          }
          '{' -> {
            listOf(JsonUtils.parse(bodyStr, Metric::class.java))
          }
          else -> {
            log.warn("无效的body: {}", bodyStr)
            emptyList()
          }
        }
        if (metrics.isEmpty()) {
          return@launch
        }
        val timeSeriesList = metrics.map { it.toTimeSeries() }
        if (log.isDebugEnabled) {
          log.debug("opentsdb上报数据 {}条", timeSeriesList.size)
        }
        timeSeriesService.saveAll(timeSeriesList)
      } catch (e: Exception) {
        log.info("保存opentsdb上报数据发生异常 {}: ", e.message)
      }
    }
  }
}

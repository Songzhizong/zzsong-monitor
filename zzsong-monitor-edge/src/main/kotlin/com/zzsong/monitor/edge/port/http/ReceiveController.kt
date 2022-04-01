package com.zzsong.monitor.edge.port.http

import cn.idealframework.compression.Gzip
import cn.idealframework.json.JsonUtils
import cn.idealframework.lang.StringUtils
import com.zzsong.monitor.common.pojo.Metric
import com.zzsong.monitor.edge.application.MetricService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.xerial.snappy.Snappy

/**
 * 指标数据上报接口
 *
 * @author 宋志宗 on 2022/3/19
 */
@RestController
class ReceiveController(private val metricService: MetricService) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(ReceiveController::class.java)
  }

  @DelicateCoroutinesApi
  @PostMapping("/opentsdb/put")
  suspend fun opentsdbPut(
    @RequestHeader(required = false, name = "Content-Encoding")
    contentEncoding: String?,
    @RequestBody(required = false)
    body: ByteArray
  ) {
    GlobalScope.launch {
      try {
        val bytes = decode(body, contentEncoding) ?: return@launch
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
        metricService.saveAll(metrics)
      } catch (e: Exception) {
        log.info("保存opentsdb上报数据发生异常 {}: ", e.message)
      }
    }
  }


  suspend fun prometheusRemoteWrite(
    @RequestHeader(required = false, name = "Content-Encoding")
    contentEncoding: String?,
    @RequestBody(required = false)
    body: ByteArray
  ) {

  }


  private fun decode(bytes: ByteArray?, contentEncoding: String?): ByteArray? {
    if (bytes == null) {
      return null
    }
    if (StringUtils.isBlank(contentEncoding)) {
      return bytes
    }
    if ("gzip".equals(contentEncoding, ignoreCase = true)) {
      return Gzip.uncompress(bytes)
    }
    if ("snappy".equals(contentEncoding, ignoreCase = true)) {
      return Snappy.uncompress(bytes)
    }
    log.error("未知的压缩方式: {}", contentEncoding)
    return bytes
  }
}

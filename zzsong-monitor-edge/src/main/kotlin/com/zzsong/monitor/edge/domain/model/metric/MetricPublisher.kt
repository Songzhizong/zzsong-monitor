package com.zzsong.monitor.edge.domain.model.metric

import cn.idealframework.extensions.reactor.Reactors
import cn.idealframework.json.JsonUtils
import com.zzsong.monitor.common.dto.req.MetricReportMessage
import com.zzsong.monitor.common.pojo.Metric
import com.zzsong.monitor.edge.configure.properties.MonitorEdgeProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.xerial.snappy.Snappy
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * @author 宋志宗 on 2022/3/20
 */
@Component
class MetricPublisher(properties: MonitorEdgeProperties) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(MetricPublisher::class.java)
    private val webClient = Reactors.webClient {
      @Suppress("UsePropertyAccessSyntax")
      it.setKeepAlive(true)
        .setResponseTimeout(Duration.ofSeconds(5))
    }
  }

  private val clusterName = properties.clusterName
  private val httpUrls = properties.publish.http.urls

  suspend fun publish(metrics: List<Metric>) {
    if (httpUrls.isEmpty()) {
      return
    }
    val message = MetricReportMessage()
    message.cluster = clusterName
    message.metrics = metrics
    val messageJson = JsonUtils.toJsonString(message)
    val messageBytes = messageJson.toByteArray(Charsets.UTF_8)
    val compress = Snappy.compress(messageBytes)
    this.httpPublish(httpUrls, compress)
  }

  private fun httpPublish(urls: List<String>, body: ByteArray) {
    for (url in urls) {
      webClient.post().uri(url)
        .header(HttpHeaders.CONTENT_ENCODING, "snappy")
        .body(BodyInserters.fromValue(body))
        .exchangeToMono { response ->
          val statusCode = response.statusCode()
          response.bodyToMono(String::class.java)
            .doOnNext { res ->
              if (!statusCode.is2xxSuccessful) {
                log.info("推送指标数据到: {} 失败: {} {}", url, statusCode.value(), res)
              }
            }
        }
        .onErrorResume {
          log.info("推送指标数据到: {} 出现异常: {} {}", url, it.javaClass.name, it.message)
          Mono.just("")
        }.subscribe()
    }
  }
}

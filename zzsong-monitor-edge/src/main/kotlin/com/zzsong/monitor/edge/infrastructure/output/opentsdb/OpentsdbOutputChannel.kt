package com.zzsong.monitor.edge.infrastructure.output.opentsdb

import cn.idealframework.compression.Gzip
import cn.idealframework.extensions.reactor.Reactors
import cn.idealframework.json.JsonUtils
import cn.idealframework.lang.Lists
import com.zzsong.monitor.common.pojo.TimeSeries
import com.zzsong.monitor.edge.infrastructure.output.OutputChannel
import com.zzsong.monitor.edge.infrastructure.output.OutputProperties
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import java.time.Duration

/**
 * @author 宋志宗 on 2022/4/26
 */
@Component
class OpentsdbOutputChannel(outputProperties: OutputProperties) : OutputChannel {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(OpentsdbOutputChannel::class.java)
  }

  private val webClient = Reactors.webClient {
    @Suppress("UsePropertyAccessSyntax")
    it.setKeepAlive(true).setResponseTimeout(Duration.ofSeconds(5))
  }
  private val urls = ArrayList<String>()

  init {
    val opentsdb = outputProperties.opentsdb
    for (properties in opentsdb) {
      val url = properties.url
      urls.add(url)
    }
  }

  override fun ready(): Boolean = urls.isNotEmpty()

  override suspend fun output(timeSeriesList: List<TimeSeries>) {
    if (Lists.isEmpty(timeSeriesList)) {
      return
    }
    val metrics = timeSeriesList.map { it.toMetric() }
    val jsonString = JsonUtils.toJsonString(metrics)
    val byteArray = jsonString.toByteArray(Charsets.UTF_8)
    val compress = Gzip.compress(byteArray)
    coroutineScope {
      urls.map { url ->
        val async = async {
          webClient.post().uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Content-Encoding", "gzip")
            .body(BodyInserters.fromValue(compress))
            .exchangeToMono { response ->
              val statusCode = response.statusCode()
              response.bodyToMono(String::class.java)
                .defaultIfEmpty("")
                .map { body ->
                  if (statusCode.is2xxSuccessful) {
                    Pair(true, body)
                  } else {
                    Pair(false, body)
                  }
                }
            }.awaitSingle()
        }
        Pair(url, async)
      }.forEach { (url, async) ->
        try {
          val (success, body) = async.await()
          if (!success) {
            log.info("opentsdb output返回失败, url: {} body: {}", url, body)
          }
        } catch (e: Exception) {
          log.info("opentsdb output出现异常, url: {} e: {}", url, e.message)
        }
      }
    }
  }
}

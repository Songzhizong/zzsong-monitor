package com.zzsong.monitor.edge.infrastructure.prometheus

import com.zzsong.monitor.common.pojo.Metric
import com.zzsong.monitor.common.prometheus.PrometheusResult
import com.zzsong.monitor.edge.configure.properties.MonitorEdgeProperties
import com.zzsong.monitor.edge.domain.model.metric.MetricStore
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * prometheus时序数据存储库
 *
 * @author 宋志宗 on 2022/3/19
 */
@Component
class PrometheusMetricStore(properties: MonitorEdgeProperties) : MetricStore {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(PrometheusMetricStore::class.java)
  }

  private val readBaseUrl: String = properties.prometheus.readBaseUrl
  private var masterWriteUrl: String = ""
  private val prometheusClient: PrometheusClient
  private val slaveWriteUrls = ArrayList<String>(properties.prometheus.writeUrls.size - 1)

  init {
    assert(readBaseUrl.isNotBlank()) {
      val message = "Prometheus read base url is blank"
      log.error(message)
      message
    }
    val writeUrls = properties.prometheus.writeUrls
    assert(writeUrls.isNotEmpty()) {
      val message = "Prometheus write urls is empty"
      log.error(message)
      message
    }
    for (writeUrl in writeUrls) {
      if (writeUrl.startsWith(readBaseUrl)) {
        masterWriteUrl = writeUrl
      } else {
        slaveWriteUrls.add(writeUrl)
      }
    }
    assert(masterWriteUrl.isNotBlank()) {
      val message = "Prometheus write urls does not contain write url"
      log.error(message)
      message
    }
    prometheusClient = PrometheusClientImpl(properties.prometheus.timeout)
  }

  @DelicateCoroutinesApi
  override suspend fun write(metrics: List<Metric>) {
    val timeSeries = metrics.map { it.toTimeSeries() }
    GlobalScope.launch(Dispatchers.Default) {
      try {
        val deferredList =
          slaveWriteUrls.map { url -> async { prometheusClient.remoteWrite(url, timeSeries) } }
        for (deferred in deferredList) {
          try {
            deferred.await()
          } catch (e: Exception) {
            log.info("保存时序数据到prometheus从库失败: {}", e.message)
          }
        }
      } catch (e: Exception) {
        log.warn("异步写入时序数据到从库出现异常{} :", e.javaClass.name, e)
      }
    }
    prometheusClient.remoteWrite(masterWriteUrl, timeSeries)
  }

  suspend fun getLabels(): PrometheusResult<List<String>> {
    return PrometheusResult.data(emptyList())
  }
}

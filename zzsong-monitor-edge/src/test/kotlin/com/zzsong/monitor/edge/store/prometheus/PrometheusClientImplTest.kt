package com.zzsong.monitor.edge.store.prometheus

import cn.idealframework.json.JsonUtils
import com.zzsong.monitor.common.pojo.TimeSeries
import com.zzsong.monitor.edge.infrastructure.prometheus.PrometheusClientImpl
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.Duration

/**
 * @author 宋志宗 on 2022/3/18
 */
class PrometheusClientImplTest {
  private val prometheusClient = PrometheusClientImpl(Duration.ofSeconds(3))
  private val baseUrl = "http://127.0.0.1:9090"


  @Test
  fun remoteWrite() = runBlocking {
    val timeSeries = TimeSeries()
      .also {
        it.sample =
          TimeSeries.Sample().apply { value = 2.0728;timestamp = System.currentTimeMillis() }
        it.labels.apply {
          add(TimeSeries.Label().apply { name = TimeSeries.NAME_LABEL;value = "cpu_usage_active" })
          add(TimeSeries.Label().apply { name = "cpu";value = "cpu-total" })
          add(TimeSeries.Label().apply { name = "ident";value = "zzsong" })
        }
      }
    prometheusClient.remoteWrite("http://127.0.0.1:9090/api/v1/write", listOf(timeSeries))
  }

  @Test
  fun metadata() = runBlocking {
    val labels = prometheusClient.metadata("http://127.0.0.1:9090")
    println(JsonUtils.toJsonString(labels))
  }

  @Test
  fun getLabels() = runBlocking {
    val labels = prometheusClient.getLabels("http://127.0.0.1:9090")
    println(JsonUtils.toJsonString(labels))
  }

  @Test
  fun labelValues() = runBlocking {
    val labelValues = prometheusClient.labelValues(
      baseUrl,
      "ident",
      "2022-03-18T07:11:32.560Z",
      "2022-03-18T19:11:32.560Z"
    )
    println(JsonUtils.toJsonString(labelValues))
  }

  @Test
  fun query() = runBlocking {
    val query = prometheusClient.query(
      baseUrl, "cpu_usage_idle{cpu=\"cpu-total\"}", 1647675052.366
    )
    println(JsonUtils.toJsonString(query))
  }

  @Test
  fun queryRange() = runBlocking {
    val queryRange = prometheusClient.queryRange(
      baseUrl,
      "cpu_usage_idle{cpu=\"cpu-total\"}",
      1647622826.77,
      1647630026.77,
      10
    )
    println(JsonUtils.toJsonString(queryRange))
  }

  @Test
  fun series() = runBlocking {
    val series = prometheusClient
      .series(baseUrl, null, null, "cpu_usage_idle")
    println(JsonUtils.toJsonString(series))
  }
}

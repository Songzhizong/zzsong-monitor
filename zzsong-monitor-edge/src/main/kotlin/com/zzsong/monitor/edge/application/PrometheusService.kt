package com.zzsong.monitor.edge.application

import cn.idealframework.transmission.exception.InternalServerException
import com.zzsong.monitor.prometheus.ReactorPrometheusClient
import com.zzsong.monitor.prometheus.data.Metadata
import com.zzsong.monitor.prometheus.data.PrometheusResult
import com.zzsong.monitor.prometheus.data.QueryResp
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * @author 宋志宗 on 2022/4/25
 */
@Service
class PrometheusService(
  private val prometheusClient: ReactorPrometheusClient?
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(PrometheusService::class.java)
  }

  suspend fun metadata(): PrometheusResult<Map<String, List<Metadata>>> {
    return getClient().metadata().awaitSingle()
  }

  suspend fun getLabels(
    match: String?,
    start: Double?,
    end: Double?
  ): PrometheusResult<List<String>> {
    return getClient().getLabels(start, end, match).awaitSingle()
  }

  suspend fun labelValues(
    label: String,
    start: Double?,
    end: Double?,
    match: String?
  ): PrometheusResult<List<String>> {
    return getClient().labelValues(label, start, end, match).awaitSingle()
  }

  suspend fun series(
    match: String,
    start: String?,
    end: String?
  ): PrometheusResult<List<Map<String, String>>> {
    return getClient().series(start, end, match).awaitSingle()
  }

  suspend fun query(query: String, time: Double?): PrometheusResult<QueryResp> {
    return getClient().query(query, time).awaitSingle()
  }

  suspend fun queryRange(
    query: String,
    start: Double,
    end: Double,
    step: Int
  ): PrometheusResult<QueryResp> {
    return getClient().queryRange(query, start, end, step).awaitSingle()
  }

  private fun getClient(): ReactorPrometheusClient {
    if (prometheusClient == null) {
      log.warn("未配置prometheus读取客户端")
      throw InternalServerException("未配置prometheus读取客户端")
    }
    return prometheusClient
  }
}

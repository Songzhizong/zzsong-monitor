package com.zzsong.monitor.edge.infrastructure.prometheus

import cn.idealframework.extensions.reactor.Reactors
import cn.idealframework.lang.StringUtils
import cn.idealframework.lang.Tuple
import com.zzsong.monitor.common.pojo.TimeSeries
import com.zzsong.monitor.common.prometheus.PrometheusMetadata
import com.zzsong.monitor.common.prometheus.PrometheusQueryRangeResp
import com.zzsong.monitor.common.prometheus.PrometheusQueryResp
import com.zzsong.monitor.common.prometheus.PrometheusResult
import com.zzsong.monitor.common.prometheus.exception.PrometheusException
import com.zzsong.monitor.edge.infrastructure.prometheus.protobuf.Remote
import com.zzsong.monitor.edge.infrastructure.prometheus.protobuf.Types
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.DefaultUriBuilderFactory
import org.xerial.snappy.Snappy
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.net.URLEncoder
import java.time.Duration

/**
 * prometheus客户端实现
 *
 * @author 宋志宗 on 2022/3/18
 */
class PrometheusClientImpl(private val timeout: Duration) : PrometheusClient {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(PrometheusClientImpl::class.java)

    private val listStringReference =
      object : ParameterizedTypeReference<PrometheusResult<List<String>>>() {}

    private val listMapReference =
      object : ParameterizedTypeReference<PrometheusResult<List<Map<String, String>>>>() {}

    private val metadataReference =
      object :
        ParameterizedTypeReference<PrometheusResult<Map<String, List<PrometheusMetadata>>>>() {}

    private val queryRangeRespReference =
      object :
        ParameterizedTypeReference<PrometheusResult<PrometheusQueryRangeResp>>() {}

    private val queryRespReference =
      object :
        ParameterizedTypeReference<PrometheusResult<PrometheusQueryResp>>() {}
  }

  private val webClient: WebClient
  private val unEncodedWebClient: WebClient

  init {
    val httpClient = Reactors.httpClient {
      @Suppress("UsePropertyAccessSyntax")
      it.setKeepAlive(true).setResponseTimeout(timeout).setCompressionEnabled(true)
    }
    webClient = Reactors.webClientBuilderOfHttpClient(httpClient).build()
    unEncodedWebClient = Reactors
      .webClientBuilderOfHttpClient(httpClient) {
        @Suppress("UsePropertyAccessSyntax")
        it.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE)
      }.build()
  }


  override suspend fun remoteWrite(url: String, timeSeries: Collection<TimeSeries>) {
    val writeRequest = genWriteRequest(timeSeries)
    val byteArray = writeRequest.toByteArray()
    val compress = Snappy.compress(byteArray)
    val res = webClient.post().uri(url)
      .body(BodyInserters.fromValue(compress))
      .exchangeToMono { res ->
        val statusCode = res.statusCode()
        res.bodyToMono(String::class.java)
          .defaultIfEmpty("")
          .map { body -> Tuple(statusCode, body) }
      }
      .awaitSingleOrNull()
    if (res == null) {
      log.warn("prometheus写入数据是返回结果为空")
      return
    }
    val httpStatus = res.first
    if (!httpStatus.is2xxSuccessful) {
      val value = httpStatus.value()
      val message = res.second
      log.info("时序数据写入prometheus返回失败 url: {} httpStatus: {} body: {}", url, value, message.trim())
    }
  }

  override suspend fun metadata(baseUrl: String): PrometheusResult<Map<String, List<PrometheusMetadata>>> {
    return webClient.get().uri("$baseUrl/api/v1/metadata")
      .exchangeToMono { response ->
        val statusCode = response.statusCode()
        response.bodyToMono(metadataReference)
          .switchIfEmpty { switchEmptyBody() }
          .map { handleResult(statusCode, it) }
      }.awaitSingle()
  }

  override suspend fun getLabels(baseUrl: String): PrometheusResult<List<String>> {
    return webClient.get().uri("$baseUrl/api/v1/labels")
      .exchangeToMono { response ->
        val statusCode = response.statusCode()
        response.bodyToMono(listStringReference)
          .switchIfEmpty { switchEmptyBody() }
          .map { handleResult(statusCode, it) }
      }.awaitSingle()
  }

  override suspend fun labelValues(
    baseUrl: String,
    label: String,
    start: String,
    end: String
  ): PrometheusResult<List<String>> {
    val url = "$baseUrl/api/v1/label/$label/values?start=$start&end=$end"
    return webClient.get().uri(url)
      .exchangeToMono { response ->
        val statusCode = response.statusCode()
        response.bodyToMono(listStringReference)
          .switchIfEmpty { switchEmptyBody() }
          .map { handleResult(statusCode, it) }
      }.awaitSingle()
  }

  override suspend fun query(
    baseUrl: String,
    query: String,
    time: Double?
  ): PrometheusResult<PrometheusQueryResp> {
    val encode = URLEncoder.encode(query, Charsets.UTF_8)
    val replace = encode.replace("%7E", "~")
    var url = "$baseUrl/api/v1/query?query=$replace"
    if (time != null) {
      url = "$url&time=$time"
    }
    return unEncodedWebClient.get().uri(url)
      .exchangeToMono { response ->
        val statusCode = response.statusCode()
        response.bodyToMono(queryRespReference)
          .switchIfEmpty { switchEmptyBody() }
          .map { handleResult(statusCode, it) }
      }.awaitSingle()
  }

  override suspend fun queryRange(
    baseUrl: String,
    query: String,
    start: Double,
    end: Double,
    step: Int
  ): PrometheusResult<PrometheusQueryRangeResp> {
    val encode = URLEncoder.encode(query, Charsets.UTF_8)
    val replace = encode.replace("%7E", "~")
    val url = "$baseUrl/api/v1/query_range?query=$replace&start=$start&end=$end&step=$step"
    return unEncodedWebClient.get().uri(url)
      .exchangeToMono { response ->
        val statusCode = response.statusCode()
        response.bodyToMono(queryRangeRespReference)
          .switchIfEmpty { switchEmptyBody() }
          .map { handleResult(statusCode, it) }
      }.awaitSingle()
  }

  override suspend fun series(
    baseUrl: String,
    start: String?,
    end: String?,
    match: String
  ): PrometheusResult<List<Map<String, String>>> {
    var fromFormData = BodyInserters.fromFormData("match[]", match)
    if (StringUtils.isNotBlank(start)) {
      fromFormData = fromFormData.with("start", start!!)
    }
    if (StringUtils.isNotBlank(start)) {
      fromFormData = fromFormData.with("end", end!!)
    }
    return webClient.post()
      .uri("$baseUrl/api/v1/series")
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .body(fromFormData)
      .exchangeToMono { response ->
        val statusCode = response.statusCode()
        response.bodyToMono(listMapReference)
          .switchIfEmpty { switchEmptyBody() }
          .map { handleResult(statusCode, it) }
      }.awaitSingle()
  }

  private fun <T> switchEmptyBody() =
    Mono.just(PrometheusResult.error<T>("empty_body", "empty body"))

  private fun <T> handleResult(
    statusCode: HttpStatus,
    res: PrometheusResult<T>
  ): PrometheusResult<T> {
    if (!statusCode.is2xxSuccessful) {
      val error = res.error
      val errorType = res.errorType
      throw PrometheusException(statusCode.value(), errorType, error)
    }
    return res
  }

  /**
   * 构造prometheus远程写入请求对象
   *
   * @author 宋志宗 on 2022/3/18
   */
  private fun genWriteRequest(timeSeries: Collection<TimeSeries>): Remote.WriteRequest {
    val series = timeSeries
      .map { metric ->
        val sample = Types.Sample.newBuilder()
          .setValue(metric.sample.value)
          .setTimestamp(metric.sample.timestamp)
          .build()
        val labels = metric.labels
          .map { label ->
            Types.Label.newBuilder()
              .setName(label.name)
              .setValue(label.value)
              .build()
          }
        Types.TimeSeries.newBuilder()
          .addSamples(sample)
          .addAllLabels(labels)
          .build()
      }
    return Remote.WriteRequest.newBuilder()
      .addAllTimeseries(series).build()
  }
}

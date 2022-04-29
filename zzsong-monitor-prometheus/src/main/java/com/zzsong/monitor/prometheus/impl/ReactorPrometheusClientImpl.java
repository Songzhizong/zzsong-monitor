package com.zzsong.monitor.prometheus.impl;

import cn.idealframework.extensions.reactor.Reactors;
import cn.idealframework.json.JsonUtils;
import cn.idealframework.json.TypeReference;
import cn.idealframework.lang.StringUtils;
import com.zzsong.monitor.prometheus.ReactorPrometheusClient;
import com.zzsong.monitor.prometheus.data.Metadata;
import com.zzsong.monitor.prometheus.data.PrometheusResult;
import com.zzsong.monitor.prometheus.data.QueryResp;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 反应式prometheus客户端实现
 *
 * @author 宋志宗 on 2022/4/20
 */
@SuppressWarnings("DuplicatedCode")
@CommonsLog
public class ReactorPrometheusClientImpl implements ReactorPrometheusClient {
  private static final TypeReference<PrometheusResult<Map<String, List<Metadata>>>>
    METADATA_TYPE_REFERENCE = new TypeReference<PrometheusResult<Map<String, List<Metadata>>>>() {
  };
  private static final TypeReference<PrometheusResult<List<String>>>
    STRING_LIST_RESULT = new TypeReference<PrometheusResult<List<String>>>() {
  };
  private static final TypeReference<PrometheusResult<List<Map<String, String>>>>
    SERIES_REFERENCE = new TypeReference<PrometheusResult<List<Map<String, String>>>>() {
  };
  private static final TypeReference<PrometheusResult<QueryResp>>
    QUERY_REFERENCE = new TypeReference<PrometheusResult<QueryResp>>() {
  };
  /** 基础读取地址 */
  private final String baseReadUrl;
  private final String metadataUrl;
  private final String labelsUrl;
  private final String seriesUrl;
  private final String queryUrl;
  private final String queryRangeUrl;
  private final WebClient webClient;
  private final WebClient unEncodedWebClient;

  public ReactorPrometheusClientImpl(@Nonnull String baseReadUrl,
                                     @Nonnull Duration timeout) {
    this.baseReadUrl = baseReadUrl;
    this.metadataUrl = baseReadUrl + "/api/v1/metadata";
    this.labelsUrl = baseReadUrl + "/api/v1/labels";
    this.seriesUrl = baseReadUrl + "/api/v1/series";
    this.queryUrl = baseReadUrl + "/api/v1/query";
    this.queryRangeUrl = baseReadUrl + "/api/v1/query_range";
    HttpClient httpClient = Reactors.httpClient(ops ->
      ops.setKeepAlive(true).setResponseTimeout(timeout).setCompressionEnabled(true)
    );
    this.webClient = Reactors.webClientBuilderOfHttpClient(httpClient,
      ops -> ops.setMaxInMemorySize(8 << 20)).build();
    this.unEncodedWebClient = Reactors
      .webClientBuilderOfHttpClient(httpClient, ops ->
        ops.setMaxInMemorySize(8 << 20)
          .setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE)
      ).build();
  }

  @Nonnull
  @Override
  public Mono<PrometheusResult<Map<String, List<Metadata>>>> metadata() {
    return webClient.get().uri(metadataUrl)
      .exchangeToMono(response -> parseResponse(response, METADATA_TYPE_REFERENCE));
  }

  @Nonnull
  @Override
  public Mono<PrometheusResult<List<String>>> getLabels(@Nullable Double start,
                                                        @Nullable Double end,
                                                        @Nullable String match) {
    // http://127.0.0.1:9091/api/v1/labels?start=1650356507&end=1650360107&match[]=cpu_usage_active{ident=~"192.168.1.195|debian185|snmp1|snmp2|zzsong"}
    StringBuilder qsb = new StringBuilder();
    if (start != null) {
      qsb.append("start=").append(start);
    }
    if (end != null) {
      if (qsb.length() > 0) {
        qsb.append("&");
      }
      qsb.append("end=").append(end);
    }
    if (StringUtils.isNotBlank(match)) {
      if (qsb.length() > 0) {
        qsb.append("&");
      }
      String encode;
      try {
        encode = encodeQueryString(match);
      } catch (UnsupportedEncodingException e) {
        log.info("encode query exception: ", e);
        return Mono.error(e);
      }
      qsb.append("match[]=").append(encode);
    }
    String queryString = qsb.toString();
    String url = labelsUrl + "?" + queryString;
    return unEncodedWebClient.get().uri(url)
      .exchangeToMono(response -> parseResponse(response, STRING_LIST_RESULT));
  }

  @Nonnull
  @Override
  public Mono<PrometheusResult<List<String>>> labelValues(@Nonnull String label,
                                                          @Nullable Double start,
                                                          @Nullable Double end,
                                                          @Nullable String match) {
    // http://127.0.0.1:9091/api/v1/label/__name__/values?end=1650360119&match[]={ident=~"192.168.1.195|debian185|snmp1|snmp2|zzsong"}&start=1650356519
    StringBuilder qsb = new StringBuilder();
    if (start != null) {
      qsb.append("start=").append(start);
    }
    if (end != null) {
      if (qsb.length() > 0) {
        qsb.append("&");
      }
      qsb.append("end=").append(end);
    }
    if (StringUtils.isNotBlank(match)) {
      if (qsb.length() > 0) {
        qsb.append("&");
      }
      String encode;
      try {
        encode = encodeQueryString(match);
      } catch (UnsupportedEncodingException e) {
        log.info("encode query exception: ", e);
        return Mono.error(e);
      }
      qsb.append("match[]=").append(encode);
    }
    String queryString = qsb.toString();
    String url = baseReadUrl + "/api/v1/label/" + label + "/values?" + queryString;
    return unEncodedWebClient.get().uri(url)
      .exchangeToMono(response -> parseResponse(response, STRING_LIST_RESULT));
  }

  @Nonnull
  @Override
  public Mono<PrometheusResult<List<Map<String, String>>>> series(@Nullable String start,
                                                                  @Nullable String end,
                                                                  @Nonnull String match) {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    if (start != null) {
      body.add("start", start);
    }
    if (end != null) {
      body.add("end", end);
    }
    body.add("match[]", match);
    return webClient.post().uri(seriesUrl)
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .body(BodyInserters.fromFormData(body))
      .exchangeToMono(response -> parseResponse(response, SERIES_REFERENCE));
  }

  @Nonnull
  @Override
  public Mono<PrometheusResult<QueryResp>> query(@Nonnull String query,
                                                 @Nullable Double time) {
    // http://127.0.0.1:9091/api/v1/query?query=cpu_usage_active{ident="zzsong1"}&time=1650425594.647

    String encode;
    try {
      encode = encodeQueryString(query);
    } catch (UnsupportedEncodingException e) {
      log.info("encode query exception: ", e);
      return Mono.error(e);
    }
    StringBuilder qsb = new StringBuilder("query=").append(encode);
    if (time != null) {
      qsb.append("&time=").append(time);
    }
    String queryString = qsb.toString();
    String url = queryUrl + "?" + queryString;
    return unEncodedWebClient.get().uri(url)
      .exchangeToMono(response -> parseResponse(response, QUERY_REFERENCE));
  }

  @Nonnull
  @Override
  public Mono<PrometheusResult<QueryResp>> queryRange(@Nonnull String query,
                                                      double start,
                                                      double end,
                                                      int step) {
    //  http://127.0.0.1:9091/api/v1/query_range?start=1650422387.344&end=1650425987.344&step=14&query=cpu_usage_active{ident="zzsong1"}
    //
    String encode;
    try {
      encode = encodeQueryString(query);
    } catch (UnsupportedEncodingException e) {
      log.info("encode query exception: ", e);
      return Mono.error(e);
    }
    String queryString = "start=" + (start)
      + "&end=" + (end) + "&step=" + step + "&query=" + encode;
    String url = queryRangeUrl + "?" + queryString;
    return unEncodedWebClient.get().uri(url)
      .exchangeToMono(response -> parseResponse(response, QUERY_REFERENCE));
  }

  @Nonnull
  private String encodeQueryString(String queryString) throws UnsupportedEncodingException {
    String encode = URLEncoder.encode(queryString, "UTF-8");
    return encode.replace("%7E", "~").replace("%5B", "[").replace("%5D", "]");
  }

  @Nonnull
  private <T> Mono<PrometheusResult<T>> parseResponse(@Nonnull ClientResponse response,
                                                      @Nonnull TypeReference<PrometheusResult<T>> reference) {
    HttpStatus httpStatus = response.statusCode();
    return response.bodyToMono(String.class)
      .defaultIfEmpty("")
      .map(body -> {
        if (StringUtils.isNotBlank(body)) {
          return JsonUtils.parse(body, reference);
        } else {
          int value = httpStatus.value();
          String valueOf = String.valueOf(value);
          log.info("prometheus返回结果为空, httpStatus = " + valueOf);
          return PrometheusResult.error("empty_body", valueOf);
        }
      });
  }
}

package com.zzsong.monitor.prometheus.impl;


import com.zzsong.monitor.prometheus.AsyncPrometheusClient;
import com.zzsong.monitor.prometheus.ReactorPrometheusClient;
import com.zzsong.monitor.prometheus.data.Metadata;
import com.zzsong.monitor.prometheus.data.PrometheusResult;
import com.zzsong.monitor.prometheus.data.QueryResp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 异步prometheus客户端实现
 *
 * @author 宋志宗 on 2022/4/20
 */
public class AsyncPrometheusClientImpl implements AsyncPrometheusClient {
  private final ReactorPrometheusClient client;

  public AsyncPrometheusClientImpl(@Nonnull String baseReadUrl,
                                   @Nonnull Duration timeout) {
    this.client = new ReactorPrometheusClientImpl(baseReadUrl, timeout);
  }

  @Nonnull
  @Override
  public CompletableFuture<PrometheusResult<Map<String, List<Metadata>>>> metadata() {
    return client.metadata().toFuture();
  }

  @Nonnull
  @Override
  public CompletableFuture<PrometheusResult<List<String>>> getLabels(@Nullable Double start,
                                                                     @Nullable Double end,
                                                                     @Nullable String match) {
    return client.getLabels(start, end, match).toFuture();
  }

  @Nonnull
  @Override
  public CompletableFuture<PrometheusResult<List<String>>> labelValues(@Nonnull String label,
                                                                       @Nullable Double start,
                                                                       @Nullable Double end,
                                                                       @Nullable String match) {
    return client.labelValues(label, start, end, match).toFuture();
  }

  @Nonnull
  @Override
  public CompletableFuture<PrometheusResult<List<Map<String, String>>>> series(@Nullable String start,
                                                                               @Nullable String end,
                                                                               @Nonnull String match) {
    return client.series(start, end, match).toFuture();
  }

  @Nonnull
  @Override
  public CompletableFuture<PrometheusResult<QueryResp>> query(@Nonnull String query,
                                                              @Nullable Double time) {
    return client.query(query, time).toFuture();
  }

  @Nonnull
  @Override
  public CompletableFuture<PrometheusResult<QueryResp>> queryRange(@Nonnull String query,
                                                                   double start,
                                                                   double end,
                                                                   int step) {
    return client.queryRange(query, start, end, step).toFuture();
  }
}

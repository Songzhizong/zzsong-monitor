package com.zzsong.monitor.prometheus.impl;

import com.zzsong.monitor.prometheus.PrometheusClient;
import com.zzsong.monitor.prometheus.ReactorPrometheusClient;
import com.zzsong.monitor.prometheus.data.Metadata;
import com.zzsong.monitor.prometheus.data.PrometheusResult;
import com.zzsong.monitor.prometheus.data.QueryResp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 同步prometheus客户端实现
 *
 * @author 宋志宗 on 2022/4/20
 */
public class PrometheusClientImpl implements PrometheusClient {
  private final ReactorPrometheusClient client;

  public PrometheusClientImpl(@Nonnull String baseReadUrl,
                              @Nonnull Duration timeout) {
    this.client = new ReactorPrometheusClientImpl(baseReadUrl, timeout);
  }

  @Nonnull
  @Override
  public PrometheusResult<Map<String, List<Metadata>>> metadata() {
    return Objects.requireNonNull(client.metadata().block());
  }

  @Nonnull
  @Override
  public PrometheusResult<List<String>> getLabels(@Nullable Double start,
                                                  @Nullable Double end,
                                                  @Nullable String match) {
    return Objects.requireNonNull(client.getLabels(start, end, match).block());
  }

  @Nonnull
  @Override
  public PrometheusResult<List<String>> labelValues(@Nonnull String label,
                                                    @Nullable Double start,
                                                    @Nullable Double end,
                                                    @Nullable String match) {
    return Objects.requireNonNull(client.labelValues(label, start, end, match).block());
  }

  @Nonnull
  @Override
  public PrometheusResult<List<Map<String, String>>> series(@Nullable String start,
                                                            @Nullable String end,
                                                            @Nonnull String match) {
    return Objects.requireNonNull(client.series(start, end, match).block());
  }

  @Nonnull
  @Override
  public PrometheusResult<QueryResp> query(@Nonnull String query,
                                           @Nullable Double time) {
    return Objects.requireNonNull(client.query(query, time).block());
  }

  @Nonnull
  @Override
  public PrometheusResult<QueryResp> queryRange(@Nonnull String query,
                                                double start,
                                                double end,
                                                int step) {
    return Objects.requireNonNull(client.queryRange(query, start, end, step).block());
  }
}

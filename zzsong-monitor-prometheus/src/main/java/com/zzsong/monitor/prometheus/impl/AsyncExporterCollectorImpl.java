package com.zzsong.monitor.prometheus.impl;

import com.zzsong.monitor.common.pojo.TimeSeries;
import com.zzsong.monitor.prometheus.AsyncExporterCollector;
import com.zzsong.monitor.prometheus.ReactorExporterCollector;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author 宋志宗 on 2022/4/22
 */
public class AsyncExporterCollectorImpl implements AsyncExporterCollector {
  private final ReactorExporterCollector collector;

  public AsyncExporterCollectorImpl(@Nonnull Duration timeout) {
    this.collector = new ReactorExporterCollectorImpl(timeout);
  }

  @Nonnull
  @Override
  public CompletableFuture<List<TimeSeries>> collect(@Nonnull String url, long timestamp) {
    return collector.collect(url, timestamp).toFuture();
  }

}

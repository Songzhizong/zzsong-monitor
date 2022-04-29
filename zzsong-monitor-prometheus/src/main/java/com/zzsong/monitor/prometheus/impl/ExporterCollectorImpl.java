package com.zzsong.monitor.prometheus.impl;

import com.zzsong.monitor.common.pojo.TimeSeries;
import com.zzsong.monitor.prometheus.ExporterCollector;
import com.zzsong.monitor.prometheus.ReactorExporterCollector;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * @author 宋志宗 on 2022/4/22
 */
public class ExporterCollectorImpl implements ExporterCollector {
  private final ReactorExporterCollector collector;

  public ExporterCollectorImpl(@Nonnull Duration timeout) {
    this.collector = new ReactorExporterCollectorImpl(timeout);
  }

  @Nonnull
  @Override
  public List<TimeSeries> collect(@Nonnull String url, long timestamp) {
    List<TimeSeries> timeSeriesList = collector.collect(url, timestamp).block();
    return timeSeriesList == null ? Collections.emptyList() : timeSeriesList;
  }
}

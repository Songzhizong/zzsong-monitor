package com.zzsong.monitor.prometheus.impl;

import com.zzsong.monitor.common.pojo.TimeSeries;
import com.zzsong.monitor.prometheus.AsyncRemoteWriter;
import com.zzsong.monitor.prometheus.ReactorRemoteWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author 宋志宗 on 2022/4/22
 */
public class AsyncRemoteWriterImpl implements AsyncRemoteWriter {
  private final ReactorRemoteWriter writer;

  public AsyncRemoteWriterImpl(@Nonnull String writeUrl, @Nonnull Duration timeout) {
    this.writer = new ReactorRemoteWriterImpl(writeUrl, timeout);
  }

  @Nonnull
  @Override
  public CompletableFuture<Boolean> remoteWrite(@Nullable List<TimeSeries> timeSeriesList) {
    return writer.remoteWrite(timeSeriesList).toFuture();
  }
}

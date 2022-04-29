package com.zzsong.monitor.prometheus.impl;

import com.zzsong.monitor.common.pojo.TimeSeries;
import com.zzsong.monitor.prometheus.ReactorRemoteWriter;
import com.zzsong.monitor.prometheus.RemoteWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;

/**
 * @author 宋志宗 on 2022/4/22
 */
public class RemoteWriterImpl implements RemoteWriter {
  private final ReactorRemoteWriter writer;

  public RemoteWriterImpl(@Nonnull String writeUrl, @Nonnull Duration timeout) {
    this.writer = new ReactorRemoteWriterImpl(writeUrl, timeout);
  }

  @Override
  public void remoteWrite(@Nullable List<TimeSeries> timeSeriesList) {
    writer.remoteWrite(timeSeriesList).block();
  }
}

package com.zzsong.monitor.prometheus;

import com.zzsong.monitor.common.pojo.TimeSeries;
import com.zzsong.monitor.prometheus.impl.ReactorRemoteWriterImpl;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;

/**
 * @author 宋志宗 on 2022/4/22
 */
public interface ReactorRemoteWriter {

  @Nonnull
  static ReactorRemoteWriter newInstance(@Nonnull String writeUrl, @Nonnull Duration timeout) {
    return new ReactorRemoteWriterImpl(writeUrl, timeout);
  }

  /**
   * 远程写入时序数据
   *
   * @param timeSeriesList 时序数据集合
   * @author 宋志宗 on 2022/4/19
   */
  @Nonnull
  Mono<Boolean> remoteWrite(@Nullable List<TimeSeries> timeSeriesList);
}

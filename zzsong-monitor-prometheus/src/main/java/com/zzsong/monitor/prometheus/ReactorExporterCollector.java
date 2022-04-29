package com.zzsong.monitor.prometheus;

import com.zzsong.monitor.common.pojo.TimeSeries;
import com.zzsong.monitor.prometheus.impl.ReactorExporterCollectorImpl;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;

/**
 * 反应式的prometheus exporter采集器
 *
 * @author 宋志宗 on 2022/4/22
 */
public interface ReactorExporterCollector {

  @Nonnull
  static ReactorExporterCollector newInstance(@Nonnull Duration timeout) {
    return new ReactorExporterCollectorImpl(timeout);
  }

  /**
   * 采集指标数据
   *
   * @param url       exporter暴露出来的采集地址
   * @param timestamp 为采集到的时序数据统一添加的时间戳
   * @return 时序数据列表
   */
  @Nonnull
  Mono<List<TimeSeries>> collect(@Nonnull String url, long timestamp);
}

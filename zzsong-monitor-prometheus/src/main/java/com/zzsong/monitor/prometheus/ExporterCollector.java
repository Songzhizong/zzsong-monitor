package com.zzsong.monitor.prometheus;

import com.zzsong.monitor.common.pojo.TimeSeries;
import com.zzsong.monitor.prometheus.impl.ExporterCollectorImpl;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;

/**
 * prometheus exporter采集器
 *
 * @author 宋志宗 on 2022/4/22
 */
public interface ExporterCollector {

  @Nonnull
  static ExporterCollector newInstance(@Nonnull Duration timeout) {
    return new ExporterCollectorImpl(timeout);
  }

  /**
   * 采集指标数据
   *
   * @param url       exporter暴露出来的采集地址
   * @param timestamp 为采集到的时序数据统一添加的时间戳
   * @return 时序数据列表
   */
  @Nonnull
  List<TimeSeries> collect(@Nonnull String url, long timestamp);
}

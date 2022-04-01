package com.zzsong.monitor.common.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.Transient;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OpenTSDB上报数据
 *
 * @author 宋志宗 on 2022/3/19
 */
@Getter
@Setter
public class Metric {
  public static final String IDENT_TAG = "ident";
  public static final String HOST_TAG = "host";

  /**
   * 指标项名称 cpu_usage_idle
   */
  @Nonnull
  private String metric;

  /**
   * 秒级时间戳 1637732157
   */
  private long timestamp;

  /**
   * 指标值 30.5
   */
  private double value;

  /** 标签 */
  @Nonnull
  private Map<String, String> tags = new LinkedHashMap<>();

  @Nonnull
  public static Metric create(@Nonnull String metric, double value, long timestamp) {
    Metric res = new Metric();
    res.setMetric(metric);
    res.setTimestamp(timestamp);
    res.setValue(value);
    return res;
  }

  @Nullable
  @Transient
  public String getIdent() {
    return getTag(IDENT_TAG);
  }

  @Nonnull
  public Metric setIdent(@Nonnull String ident) {
    return putTag(IDENT_TAG, ident);
  }

  @Nullable
  public String getTag(@Nonnull String name) {
    return getTags().get(name);
  }

  @Nonnull
  public Metric putTag(@Nonnull String name, @Nonnull String value) {
    getTags().put(name, value);
    return this;
  }

  @Nullable
  public String removeTag(@Nonnull String name) {
    return getTags().remove(name);
  }

  @Nonnull
  public TimeSeries toTimeSeries() {
    TimeSeries timeSeries = TimeSeries.create(metric, value, timestamp * 1000);
    tags.forEach(timeSeries::addLabel);
    return timeSeries;
  }
}

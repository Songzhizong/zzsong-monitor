package com.zzsong.monitor.common.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 时序数据
 *
 * @author 宋志宗 on 2022/3/18
 */
@Getter
@Setter
public class TimeSeries implements Comparable<TimeSeries> {
  public static final String NAME_LABEL = "__name__";

  /** 样本 */
  @Nonnull
  private Sample sample;

  /** 标签 */
  @Nonnull
  private Set<Label> labels = new LinkedHashSet<>();

  @Nonnull
  public static TimeSeries create(@Nonnull String name, double value, long timestamp) {
    Sample sample = Sample.of(value, timestamp);
    TimeSeries timeSeries = new TimeSeries();
    timeSeries.setSample(sample);
    timeSeries.setName(name);
    return timeSeries;
  }

  @Nonnull
  public TimeSeries setName(@Nonnull String name) {
    return addLabel(NAME_LABEL, name);
  }

  @Nonnull
  public TimeSeries addLabel(@Nonnull String name, @Nonnull String value) {
    this.getLabels().add(Label.of(name, value));
    return this;
  }

  @Override
  public int compareTo(@Nonnull TimeSeries timeSeries) {
    long timestamp = this.getSample().getTimestamp();
    long otherTimestamp = timeSeries.getSample().timestamp;
    if (timestamp == otherTimestamp) {
      return 0;
    }
    return otherTimestamp - timestamp > 0 ? 1 : -1;
  }

  @Getter
  @Setter
  @Accessors(chain = true)
  public static class Label {
    /** 名称 */
    @Nonnull
    private String name;

    /** 值 */
    @Nonnull
    private String value;

    @Nonnull
    public static Label of(@Nonnull String name, @Nonnull String value) {
      return new Label().setName(name).setValue(value);
    }
  }

  /**
   * 时序样本
   *
   * @author 宋志宗 on 2022/3/18
   */
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class Sample {

    /** 样本 */
    private double value;

    /** 毫秒时间戳 */
    private long timestamp;

    @Nonnull
    public static Sample of(double value, long timestamp) {
      return new Sample().setValue(value).setTimestamp(timestamp);
    }
  }


}

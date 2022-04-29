package com.zzsong.monitor.common.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.Transient;
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
  private transient String name = null;

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

  @Nullable
  @Transient
  public String getName() {
    if (name != null) {
      return name;
    }
    for (Label label : labels) {
      String labelName = label.getName();
      if (NAME_LABEL.equals(labelName)) {
        name = label.getValue();
        break;
      }
    }
    return name;
  }

  @Nonnull
  public TimeSeries setName(@Nonnull String name) {
    this.name = name;
    return addLabel(NAME_LABEL, name);
  }

  @Nonnull
  public TimeSeries addLabel(@Nonnull String name, @Nonnull String value) {
    this.getLabels().add(Label.of(name, value));
    return this;
  }

  @Nonnull
  public TimeSeries addLabel(@Nonnull Label label) {
    this.getLabels().add(label);
    return this;
  }

  @Nonnull
  public TimeSeries addLabels(@Nonnull Set<Label> labels) {
    this.getLabels().addAll(labels);
    return this;
  }

  @Nonnull
  public Metric toMetric() {
    Metric metric = new Metric();
    Sample sample = this.getSample();
    long timestamp = sample.getTimestamp();
    double value = sample.getValue();
    metric.setTimestamp(timestamp / 1000);
    metric.setValue(value);
    Set<Label> labels = this.getLabels();
    for (Label label : labels) {
      String labelName = label.getName();
      String labelValue = label.getValue();
      if (NAME_LABEL.equals(labelName)) {
        metric.setMetric(labelValue);
        continue;
      }
      metric.putTag(labelName, labelValue);
    }
    return metric;
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

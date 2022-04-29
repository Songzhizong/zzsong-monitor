package com.zzsong.monitor.prometheus.util;

import cn.idealframework.lang.Lists;
import com.zzsong.monitor.common.pojo.Label;
import com.zzsong.monitor.common.pojo.TimeSeries;
import com.zzsong.monitor.prometheus.protobuf.Remote;
import com.zzsong.monitor.prometheus.protobuf.Types;
import lombok.extern.apachecommons.CommonsLog;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author 宋志宗 on 2022/4/22
 */
@CommonsLog
public class PrometheusProtoUtils {

  /**
   * 将时序数据转换为prometheus远程写入数据结构
   *
   * @param timeSeriesList 时序数据列表
   * @return {@link Remote.WriteRequest}
   */
  @Nonnull
  public static Remote.WriteRequest toWriteRequest(@Nonnull List<TimeSeries> timeSeriesList) {
    List<Types.TimeSeries> seriesList = new ArrayList<>();
    for (TimeSeries series : timeSeriesList) {
      TimeSeries.Sample sample = series.getSample();
      double sampleValue = sample.getValue();
      long sampleTimestamp = sample.getTimestamp();
      Types.Sample typesSample = Types.Sample.newBuilder()
        .setValue(sampleValue).setTimestamp(sampleTimestamp).build();
      List<Types.Label> labelList = new ArrayList<>();
      Set<Label> labels = series.getLabels();
      for (Label label : labels) {
        Types.Label typesLabel = Types.Label.newBuilder()
          .setName(label.getName()).setValue(label.getValue()).build();
        labelList.add(typesLabel);
      }
      Types.TimeSeries timeSeries = Types.TimeSeries.newBuilder()
        .addSamples(typesSample).addAllLabels(labelList).build();
      seriesList.add(timeSeries);
    }
    return Remote.WriteRequest.newBuilder().addAllTimeseries(seriesList).build();
  }

  @Nonnull
  public static List<TimeSeries> parseWriteRequest(@Nonnull Remote.WriteRequest writeRequest) {
    List<Types.TimeSeries> list = writeRequest.getTimeseriesList();
    if (Lists.isEmpty(list)) {
      return Collections.emptyList();
    }
    List<TimeSeries> result = new ArrayList<>();
    for (Types.TimeSeries series : list) {
      List<Types.Label> labelsList = series.getLabelsList();
      List<Types.Sample> samplesList = series.getSamplesList();
      if (Lists.isEmpty(labelsList) || Lists.isEmpty(samplesList)) {
        log.info("样本或者标签列表为空");
        continue;
      }
      int size = samplesList.size();
      if (log.isInfoEnabled() && size > 1) {
        log.info("WriteRequest包含多个样本数据: " + size);
      }
      TimeSeries timeSeries = new TimeSeries();
      Types.Sample sample = samplesList.get(0);
      timeSeries.setSample(TimeSeries.Sample.of(sample.getValue(), sample.getTimestamp()));
      for (Types.Label label : labelsList) {
        timeSeries.addLabel(label.getName(), label.getValue());
      }
      result.add(timeSeries);
    }
    return result;
  }
}

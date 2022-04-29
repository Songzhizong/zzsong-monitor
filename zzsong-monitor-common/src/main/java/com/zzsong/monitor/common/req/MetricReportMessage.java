package com.zzsong.monitor.common.req;

import com.zzsong.monitor.common.pojo.Metric;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * 指标上报消息
 *
 * @author 宋志宗 on 2022/3/26
 */
@Getter
@Setter
public class MetricReportMessage {
  @Nonnull
  private String cluster = "";

  @Nonnull
  private List<Metric> metrics = Collections.emptyList();
}

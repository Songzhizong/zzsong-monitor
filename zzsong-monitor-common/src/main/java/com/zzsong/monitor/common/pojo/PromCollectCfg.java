package com.zzsong.monitor.common.pojo;

import cn.idealframework.util.Asserts;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author 宋志宗 on 2022/4/22
 */
@Getter
@Setter
public class PromCollectCfg {

  /** prometheus exporter的地址列表 */
  @Nonnull
  private Set<String> exporterUrls;

  /** 附加标签 */
  @Nullable
  private Set<String> appendTags;

  /** 指定指标前缀进行采集 */
  @Nullable
  private Set<String> metricPrefixes = null;

  /** 忽略的指标名称前缀 */
  @Nullable
  private Set<String> ignoreMetricPrefixes = null;

  /** 采集频率, 单位秒 */
  private long frequency = 15;

  public void check() {
    Asserts.notEmpty(exporterUrls, "exporterUrls为空");
  }
}

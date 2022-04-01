package com.zzsong.monitor.common.prometheus;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 宋志宗 on 2022/3/19
 */
@Getter
@Setter
public class PrometheusMetadata {
  private String type;
  private String help;
  private String unit;
}

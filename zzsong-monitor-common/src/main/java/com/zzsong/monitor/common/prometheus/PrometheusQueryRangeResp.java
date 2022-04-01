package com.zzsong.monitor.common.prometheus;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 宋志宗 on 2022/3/19
 */
@Getter
@Setter
public class PrometheusQueryRangeResp {
  private String resultType;

  @Nonnull
  private List<MatrixResult> result = Collections.emptyList();

  @Getter
  @Setter
  public static class MatrixResult {
    /** 指标信息: {"__name__": "cpu_usage_idle","cpu": "cpu-total","host": "songzhizongdeMacBook-Pro.local"} */
    @Nonnull
    private Map<String, String> metric = Collections.emptyMap();

    /** 指标数据: [[1647627212.960,"87.91912721453417"]] */
    @Nonnull
    private List<List<Object>> values = Collections.emptyList();
  }
}

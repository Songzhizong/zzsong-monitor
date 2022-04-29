package com.zzsong.monitor.prometheus.data;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗 on 2022/3/19
 */
@Getter
@Setter
public class QueryResp {
  /** 响应类型 */
  private String resultType;

  @Nonnull
  private List<Object> result;
}

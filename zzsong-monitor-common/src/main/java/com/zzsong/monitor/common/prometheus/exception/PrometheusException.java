package com.zzsong.monitor.common.prometheus.exception;

import lombok.Getter;

import javax.annotation.Nullable;

/**
 * prometheus接口异常
 *
 * @author 宋志宗 on 2022/3/19
 */
@Getter
public class PrometheusException extends RuntimeException {
  private final int httpStatus;
  @Nullable
  private final String errorType;
  @Nullable
  private final String error;

  public PrometheusException(int httpStatus,
                             @Nullable String errorType,
                             @Nullable String error) {
    super(error);
    this.httpStatus = httpStatus;
    this.errorType = errorType;
    this.error = error;
  }
}

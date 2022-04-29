package com.zzsong.monitor.prometheus.data;

import cn.idealframework.lang.StringUtils;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.Transient;

/**
 * prometheus接口的返回结果
 *
 * @author 宋志宗 on 2022/4/19
 */
@Getter
@Setter
public class PrometheusResult<T> {
  /** 状态 success & error */
  private String status;

  /** 错误类型 */
  @Nullable
  private String errorType;

  /** 错误描述 */
  @Nullable
  private String error;

  /** 返回结果 */
  @Nullable
  private T data;

  @Nonnull
  public static <T> PrometheusResult<T> data(@Nullable T data) {
    PrometheusResult<T> result = new PrometheusResult<>();
    result.setStatus("success");
    result.setData(data);
    return result;
  }

  @Nonnull
  public static <T> PrometheusResult<T> error(@Nonnull String type, @Nonnull String error) {
    PrometheusResult<T> result = new PrometheusResult<>();
    result.setStatus("error");
    result.setErrorType(type);
    result.setError(error);
    return result;
  }

  @Nonnull
  public static <T> PrometheusResult<T> exception(@Nonnull Throwable throwable) {
    PrometheusResult<T> result = new PrometheusResult<>();
    result.setStatus("error");
    result.setErrorType("exception");
    String message = throwable.getMessage();
    if (StringUtils.isBlank(message)) {
      message = throwable.getClass().getName();
    }
    result.setError(message);
    return result;
  }

  @Transient
  public boolean isSuccess() {
    return "success".equalsIgnoreCase(getStatus());
  }

  @Transient
  public boolean isFailure() {
    return !isSuccess();
  }

  @Transient
  public String getErrorDetails() {
    return errorType + ": " + error;
  }
}

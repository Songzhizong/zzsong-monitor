/*
 * Copyright 2021 cn.idealframework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zzsong.monitor.center.launcher;

import cn.idealframework.json.JsonFormatException;
import cn.idealframework.json.JsonParseException;
import cn.idealframework.trace.TraceContextHolder;
import cn.idealframework.transmission.BasicResult;
import cn.idealframework.transmission.ResMsg;
import cn.idealframework.transmission.Result;
import cn.idealframework.transmission.exception.VisibleException;
import cn.idealframework.transmission.exception.VisibleRuntimeException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2021/7/4
 */
@SuppressWarnings({"DuplicatedCode", "DefaultAnnotationParam"})
@Order(Ordered.LOWEST_PRECEDENCE)
@Configuration
@ControllerAdvice
@ConditionalOnExpression("${ideal.web.enable-unified-exception-handler:true}")
public class GeneralExceptionHandlerAdvice {
  private static final Log log = LogFactory.getLog(GeneralExceptionHandlerAdvice.class);
  private static final MultiValueMap<String, String> RESPONSE_HEADERS = new LinkedMultiValueMap<>();

  static {
    RESPONSE_HEADERS.set("Content-Type", "application/json;charset=utf-8");
  }

  @Nonnull
  @ExceptionHandler(VisibleRuntimeException.class)
  public ResponseEntity<Object> visibleRuntimeExceptionHandler(
    @Nonnull VisibleRuntimeException ex) {
    String message = ex.getMessage();
    int httpStatus = ex.httpStatus();
    log.info(message);
    Result<Object> body = Result.exception(ex);
    body.setHttpStatus(httpStatus);
    TraceContextHolder.current().ifPresent(context -> body.setTraceId(context.getTraceId()));
    return new ResponseEntity<>(body, RESPONSE_HEADERS, HttpStatus.valueOf(httpStatus));
  }

  @Nonnull
  @ExceptionHandler(VisibleException.class)
  public ResponseEntity<Object> visibleExceptionHandler(@Nonnull VisibleException ex) {
    String message = ex.getMessage();
    log.info(message);
    int httpStatus = ex.httpStatus();
    Result<Object> body = Result.exception(ex);
    body.setHttpStatus(httpStatus);
    TraceContextHolder.current().ifPresent(context -> body.setTraceId(context.getTraceId()));
    return new ResponseEntity<>(body, RESPONSE_HEADERS, HttpStatus.valueOf(httpStatus));
  }

  @Nonnull
  @ExceptionHandler(JsonFormatException.class)
  public ResponseEntity<Object> jsonFormatExceptionHandler(@Nonnull JsonFormatException ex) {
    log.info("JsonFormatException: ", ex);
    Result<Object> body = Result.exception(ex);
    body.setHttpStatus(500);
    TraceContextHolder.current().ifPresent(context -> body.setTraceId(context.getTraceId()));
    return new ResponseEntity<>(body, RESPONSE_HEADERS, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Nonnull
  @ExceptionHandler(JsonParseException.class)
  public ResponseEntity<Object> jsonParseExceptionHandler(@Nonnull JsonParseException ex) {
    log.info("JsonParseException: ", ex);
    Result<Object> body = Result.exception(ex);
    body.setHttpStatus(500);
    TraceContextHolder.current().ifPresent(context -> body.setTraceId(context.getTraceId()));
    return new ResponseEntity<>(body, RESPONSE_HEADERS, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * 参数校验不通过异常处理
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> methodArgumentNotValidExceptionHandler(
    @Nonnull MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream()
      .map(DefaultMessageSourceResolvable::getDefaultMessage)
      .collect(Collectors.joining(", "));
    log.info("@Valid fail : " + message);
    BasicResult res = new BasicResult();
    res.setSuccess(false);
    res.setHttpStatus(ResMsg.BAD_REQUEST.httpStatus());
    res.setCode(ResMsg.BAD_REQUEST.code());
    res.setMessage(message);
    TraceContextHolder.current().ifPresent(context -> res.setTraceId(context.getTraceId()));
    return new ResponseEntity<>(res, RESPONSE_HEADERS, HttpStatus.BAD_REQUEST);
  }


  /**
   * 参数校验不通过异常处理
   */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<Object> bindExceptionHandler(@Nonnull BindException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream()
      .map(DefaultMessageSourceResolvable::getDefaultMessage)
      .collect(Collectors.joining(", "));
    log.info("@Valid fail : " + message);
    BasicResult res = new BasicResult();
    res.setSuccess(false);
    res.setHttpStatus(ResMsg.BAD_REQUEST.httpStatus());
    res.setCode(ResMsg.BAD_REQUEST.code());
    res.setMessage(message);
    TraceContextHolder.current().ifPresent(context -> res.setTraceId(context.getTraceId()));
    return new ResponseEntity<>(res, RESPONSE_HEADERS, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Object> httpMessageNotReadableExceptionHandler(
    @Nonnull HttpMessageNotReadableException exception) {
    Throwable rootCause = exception.getRootCause();
    String originalMessage;
    if (rootCause instanceof InvalidFormatException) {
      originalMessage = ((InvalidFormatException) rootCause).getOriginalMessage();
    } else if (rootCause != null && rootCause.getMessage() != null) {
      originalMessage = rootCause.getMessage();
    } else {
      originalMessage = exception.getMessage();
    }
    if (originalMessage == null) {
      originalMessage = "HttpMessageNotReadableException";
    }
    log.info(originalMessage);
    BasicResult res = new BasicResult();
    res.setSuccess(false);
    res.setHttpStatus(ResMsg.BAD_REQUEST.httpStatus());
    res.setCode(ResMsg.BAD_REQUEST.code());
    res.setMessage(originalMessage);
    String prefix = "Cannot coerce empty String";
    if (originalMessage.startsWith(prefix)) {
      res.setMessage("枚举类型值为空时请传null,而非空白字符串");
    }
    TraceContextHolder.current().ifPresent(context -> res.setTraceId(context.getTraceId()));
    return new ResponseEntity<>(res, RESPONSE_HEADERS, HttpStatus.BAD_REQUEST);
  }

  @Nonnull
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> illegalArgumentExceptionExceptionHandler(
    @Nonnull IllegalArgumentException ex) {
    String message = ex.getLocalizedMessage();
    if (message == null) {
      message = "illegal argument";
    }
    log.info("",ex);
    BasicResult res = new BasicResult();
    res.setSuccess(false);
    res.setHttpStatus(ResMsg.BAD_REQUEST.httpStatus());
    res.setCode(ResMsg.BAD_REQUEST.code());
    res.setMessage(message);
    TraceContextHolder.current().ifPresent(context -> res.setTraceId(context.getTraceId()));
    return new ResponseEntity<>(res, RESPONSE_HEADERS, HttpStatus.BAD_REQUEST);
  }

  @Nonnull
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Object> illegalStatusExceptionExceptionHandler(
    @Nonnull IllegalStateException ex) {
    String message = ex.getMessage();
    if (message == null) {
      message = "illegal status";
    }
    log.info("", ex);
    BasicResult res = new BasicResult();
    res.setSuccess(false);
    res.setHttpStatus(ResMsg.BAD_REQUEST.httpStatus());
    res.setCode(ResMsg.BAD_REQUEST.code());
    res.setMessage(message);
    TraceContextHolder.current().ifPresent(context -> res.setTraceId(context.getTraceId()));
    return new ResponseEntity<>(res, RESPONSE_HEADERS, HttpStatus.BAD_REQUEST);
  }

  @Nonnull
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Object> methodArgumentTypeMismatchExceptionHandler(
    @Nonnull MethodArgumentTypeMismatchException ex) {
    String message = ex.getMessage();
    if (message == null) {
      message = ex.getClass().getName();
    }
    log.info(message);
    BasicResult res = new BasicResult();
    res.setSuccess(false);
    res.setHttpStatus(ResMsg.BAD_REQUEST.httpStatus());
    res.setCode(ResMsg.BAD_REQUEST.code());
    res.setMessage(message);
    TraceContextHolder.current().ifPresent(context -> res.setTraceId(context.getTraceId()));
    return new ResponseEntity<>(res, RESPONSE_HEADERS, HttpStatus.BAD_REQUEST);
  }

  /**
   * 处理其他异常信息
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> globalExceptionHandler(Exception exception) {
    log.warn("Exception -> ", exception);
    String message;
    if (exception.getMessage() != null) {
      message = exception.getMessage();
    } else {
      message = exception.getClass().getSimpleName();
    }
    BasicResult res = new BasicResult();
    res.setSuccess(false);
    res.setHttpStatus(ResMsg.INTERNAL_SERVER_ERROR.httpStatus());
    res.setCode(ResMsg.INTERNAL_SERVER_ERROR.code());
    res.setMessage(message);
    TraceContextHolder.current().ifPresent(context -> res.setTraceId(context.getTraceId()));
    return new ResponseEntity<>(res, RESPONSE_HEADERS, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}

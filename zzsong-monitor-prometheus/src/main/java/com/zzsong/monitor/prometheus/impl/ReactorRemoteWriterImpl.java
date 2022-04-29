package com.zzsong.monitor.prometheus.impl;

import cn.idealframework.extensions.reactor.Reactors;
import cn.idealframework.lang.Lists;
import com.zzsong.monitor.common.pojo.TimeSeries;
import com.zzsong.monitor.prometheus.ReactorRemoteWriter;
import com.zzsong.monitor.prometheus.protobuf.Remote;
import com.zzsong.monitor.prometheus.util.PrometheusProtoUtils;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.xerial.snappy.Snappy;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * @author 宋志宗 on 2022/4/22
 */
@CommonsLog
public class ReactorRemoteWriterImpl implements ReactorRemoteWriter {
  private final String writeUrl;
  private final WebClient webClient;


  public ReactorRemoteWriterImpl(@Nonnull String writeUrl, @Nonnull Duration timeout) {
    this.writeUrl = writeUrl;
    this.webClient = Reactors.webClient(ops ->
      ops.setKeepAlive(true).setResponseTimeout(timeout)
    );
  }

  @Nonnull
  @Override
  public Mono<Boolean> remoteWrite(@Nullable List<TimeSeries> timeSeriesList) {
    if (Lists.isEmpty(timeSeriesList)) {
      return Mono.just(true);
    }
    Remote.WriteRequest writeRequest = PrometheusProtoUtils.toWriteRequest(timeSeriesList);
    byte[] bytes = writeRequest.toByteArray();
    byte[] compress;
    try {
      compress = Snappy.compress(bytes);
    } catch (IOException e) {
      log.info("Snappy压缩出现异常: ", e);
      return Mono.error(e);
    }
    return webClient.post().uri(writeUrl)
      .body(BodyInserters.fromValue(compress))
      .exchangeToMono(response -> {
        HttpStatus httpStatus = response.statusCode();
        return response.bodyToMono(String.class)
          .defaultIfEmpty("")
          .map(body -> {
            if (httpStatus.is2xxSuccessful()) {
              return true;
            } else {
              int value = httpStatus.value();
              log.info("remote write返回失败, httpStatus = " + value + "url = " + writeUrl + " body = " + body);
              return false;
            }
          });
      })
      .onErrorResume(throwable -> {
        log.info("remote write出现异常url = " + writeUrl + " e:", throwable);
        return Mono.just(false);
      });
  }
}

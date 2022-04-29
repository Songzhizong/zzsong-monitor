package com.zzsong.monitor.prometheus.impl;

import cn.idealframework.extensions.reactor.Reactors;
import cn.idealframework.lang.StringUtils;
import com.zzsong.monitor.common.pojo.TimeSeries;
import com.zzsong.monitor.prometheus.ReactorExporterCollector;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author 宋志宗 on 2022/4/22
 */
@CommonsLog
public class ReactorExporterCollectorImpl implements ReactorExporterCollector {
  private final WebClient webClient;

  public ReactorExporterCollectorImpl(@Nonnull Duration timeout) {
    this.webClient = Reactors.webClient(ops ->
      ops.setKeepAlive(true).setResponseTimeout(timeout).setCompressionEnabled(true)
    );
  }

  @Nonnull
  @Override
  public Mono<List<TimeSeries>> collect(@Nonnull String url, long timestamp) {
    return webClient.get().uri(url)
      .exchangeToMono(response -> {
        HttpStatus httpStatus = response.statusCode();
        if (httpStatus.is2xxSuccessful()) {
          return response.bodyToMono(String.class)
            .switchIfEmpty(Mono.create(s -> {
              log.info("获取指标数据返回body为空 url: " + url);
              s.success("");
            }));
        } else {
          log.info("获取指标数据返回状态码: " + httpStatus.value() + " url: " + url);
          return Mono.just("");
        }
      })
      .map(body -> {
        if (StringUtils.isBlank(body)) {
          return Collections.emptyList();
        }
        String[] lines = StringUtils.split(body, "\r\n");
        List<TimeSeries> timeSeriesList = new ArrayList<>();
        for (String line : lines) {
          if (StringUtils.isBlank(line)) {
            continue;
          }
          line = line.trim();
          if (line.charAt(0) == '#') {
            continue;
          }
          String[] split = StringUtils.split(line, ' ');
          if (split.length != 2) {
            continue;
          }
          String s = split[0];
          String valueStr = split[1];
          double value = Double.parseDouble(valueStr);
          String[] strings = StringUtils.split(s, '{');
          String name = strings[0];
          TimeSeries timeSeries = TimeSeries.create(name, value, timestamp);
          timeSeriesList.add(timeSeries);
          if (strings.length == 2) {
            String string = strings[1];
            String labelsStr = string.substring(0, string.length() - 1);
            String[] labelStrings = StringUtils.split(labelsStr, ',');
            for (String labelString : labelStrings) {
              String[] labelSplit = StringUtils.split(labelString, '=');
              if (labelSplit.length == 2) {
                String labelName = labelSplit[0];
                String unDecodeLabelValue = labelSplit[1];
                int end = unDecodeLabelValue.length() - 1;
                String labelValue = StringUtils.substring(unDecodeLabelValue, 1, end);
                timeSeries.addLabel(labelName, labelValue);
              }
            }
          }
        }
        return timeSeriesList;
      });
  }
}

package com.zzsong.monitor.prometheus.impl;

import cn.idealframework.json.JsonUtils;
import com.zzsong.monitor.common.pojo.TimeSeries;
import com.zzsong.monitor.prometheus.ReactorExporterCollector;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;
import java.util.List;

/**
 * @author 宋志宗 on 2022/4/22
 */
@Ignore
public class ReactorExporterCollectorImplTest {
  private final ReactorExporterCollector collector = new ReactorExporterCollectorImpl(Duration.ofSeconds(5));

  @Test
  public void collect() {
    String url = "http://127.0.0.1:9104/metrics";
    long currentTimeMillis = System.currentTimeMillis();
    List<TimeSeries> block = collector.collect(url, currentTimeMillis).block();
    assert block != null;
    System.out.println(JsonUtils.toJsonString(block));
  }
}

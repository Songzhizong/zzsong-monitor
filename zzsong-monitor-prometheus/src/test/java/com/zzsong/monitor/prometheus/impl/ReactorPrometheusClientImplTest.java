package com.zzsong.monitor.prometheus.impl;

import cn.idealframework.date.DateTimes;
import cn.idealframework.json.JsonUtils;
import com.zzsong.monitor.prometheus.ReactorPrometheusClient;
import com.zzsong.monitor.prometheus.data.Metadata;
import com.zzsong.monitor.prometheus.data.PrometheusResult;
import com.zzsong.monitor.prometheus.data.QueryResp;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author 宋志宗 on 2022/4/20
 */
@Ignore
public class ReactorPrometheusClientImplTest {
  private final ReactorPrometheusClient client = new ReactorPrometheusClientImpl(
    "http://127.0.0.1:31206/edge/monitor/prometheus",
    Duration.ofSeconds(5)
  );

  @Test
  public void metadata() {
    PrometheusResult<Map<String, List<Metadata>>> block = client.metadata().block();
    assert block != null;
    System.out.printf(JsonUtils.toPrettyJsonStringIgnoreNull(block));
  }

  @Test
  public void getLabels() {
    // http://127.0.0.1:9091/api/v1/labels?start=1650356507&end=1650360107&match[]=cpu_usage_active{ident=~"192.168.1.195|debian185|snmp1|snmp2|zzsong"}
    Double start = null;
    Double end = null;
    String match = "cpu_usage_active{ident=~\"192.168.1.195|debian185|snmp1|snmp2|zzsong\"}";
    PrometheusResult<List<String>> block = client.getLabels(start, end, match).block();
    assert block != null;
    System.out.printf(JsonUtils.toPrettyJsonStringIgnoreNull(block));
  }

  @Test
  public void labelValues() {
    // http://127.0.0.1:9091/api/v1/label/__name__/values?end=1650360119&match[]={ident=~"192.168.1.195|debian185|snmp1|snmp2|zzsong"}&start=1650356519
    String label = "__name__";
    Double start = null;
    Double end = null;
    String match = "{ident=~\"192.168.1.195|debian185|snmp1|snmp2|zzsong\"}";
    PrometheusResult<List<String>> block = client.labelValues(label, start, end, match).block();
    assert block != null;
    System.out.println(JsonUtils.toPrettyJsonString(block));
  }

  @Test
  public void series() {
    LocalDateTime now = DateTimes.now();
    LocalDateTime start = now.minusHours(24);
    String startStr = DateTimes.format(start, DateTimes.TZ_DATE_TIME);
    String endStr = DateTimes.format(now, DateTimes.TZ_DATE_TIME);
    String match = "{ident=~\"192.168.1.195|debian185|snmp1|snmp2|zzsong\"}";
    PrometheusResult<List<Map<String, String>>> block = client.series(startStr, endStr, match).block();
    assert block != null;
    System.out.println(JsonUtils.toPrettyJsonString(block));
  }

  @Test
  public void query() {
    PrometheusResult<QueryResp> block = client.query("cpu_usage_active", null).block();
    assert block != null;
    System.out.println(JsonUtils.toPrettyJsonString(block));
  }

  @Test
  public void queryRange() {
    double end = System.currentTimeMillis() / 1000D;
    double start = end - 3600;
    PrometheusResult<QueryResp> block = client.queryRange("cpu_usage_active", start, end, 5).block();
    assert block != null;
    System.out.println(JsonUtils.toPrettyJsonString(block));
  }
}

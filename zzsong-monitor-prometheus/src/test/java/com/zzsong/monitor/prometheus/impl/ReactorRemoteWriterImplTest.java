package com.zzsong.monitor.prometheus.impl;

import cn.idealframework.lang.Lists;
import com.zzsong.monitor.common.pojo.TimeSeries;
import com.zzsong.monitor.prometheus.ReactorRemoteWriter;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author 宋志宗 on 2022/4/22
 */
@Ignore
public class ReactorRemoteWriterImplTest {
  private final ReactorRemoteWriter writer = new ReactorRemoteWriterImpl("http://127.0.0.1:9091/api/v1/write", Duration.ofSeconds(5));

  @Test
  public void remoteWrite() {
    double value = ThreadLocalRandom.current().nextInt(5000) / 100D;
    long timestamp = System.currentTimeMillis();
    TimeSeries series = TimeSeries.create("cpu_usage_active", value, timestamp)
      .addLabel("cpu", "cpu-total").addLabel("ident", "zzsong1");
    writer.remoteWrite(Lists.of(series)).block();
  }
}

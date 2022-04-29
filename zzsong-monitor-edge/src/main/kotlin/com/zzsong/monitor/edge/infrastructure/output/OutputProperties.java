package com.zzsong.monitor.edge.infrastructure.output;

import com.zzsong.monitor.edge.infrastructure.output.http.HttpOutputProperties;
import com.zzsong.monitor.edge.infrastructure.output.kafka.KafkaOutputProperties;
import com.zzsong.monitor.edge.infrastructure.output.opentsdb.OpentsdbOutputProperties;
import com.zzsong.monitor.edge.infrastructure.output.promethues.PrometheusOutputProperties;
import com.zzsong.monitor.edge.infrastructure.output.rabbit.RabbitOutputProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * 指标数据写出配置
 *
 * @author 宋志宗 on 2022/4/26
 */
@Getter
@Setter
@Component
@ConfigurationProperties("monitor.edge.output")
public class OutputProperties {

  /** http数据写出配置 */
  @Nonnull
  private List<HttpOutputProperties> http = new ArrayList<>();

  /** kafka数据写出配置 */
  @Nonnull
  private List<KafkaOutputProperties> kafka = new ArrayList<>();

  /** rabbitmq数据写出配置 */
  @Nonnull
  private List<RabbitOutputProperties> rabbit = new ArrayList<>();

  /** opentsdb协议数据写出配置 */
  @Nonnull
  private List<OpentsdbOutputProperties> opentsdb = new ArrayList<>();

  /** prometheus远程写入配置 */
  @Nonnull
  private List<PrometheusOutputProperties> prometheus = new ArrayList<>();
}

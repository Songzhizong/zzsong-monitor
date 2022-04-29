package com.zzsong.monitor.edge.configure;

import com.zzsong.monitor.edge.configure.properties.ReadProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2022/4/22
 */
@Getter
@Setter
@ConfigurationProperties("monitor.edge")
public class MonitorEdgeProperties {

  @Nonnull
  private String cachePrefix = "monitor:edge";

  /** 集群编码 */
  @Nonnull
  private String cluster = "";

  /** 中心节点基础访问地址 */
  @Nonnull
  private String centerBaseUrl = "";

  /** 指标数据读取配置 */
  @Nonnull
  @NestedConfigurationProperty
  private ReadProperties read = new ReadProperties();
}

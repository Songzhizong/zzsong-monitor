package com.zzsong.monitor.center.configure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 宋志宗 on 2022/3/25
 */
@Getter
@Setter
@ConfigurationProperties("monitor.center")
public class MonitorCenterProperties {

  /** 缓存前缀 */
  private String cacheProperties = "monitor:cache";
}

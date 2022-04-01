package com.zzsong.monitor.center.launcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 宋志宗 on 2022/3/19
 */
@SpringBootApplication
public class MonitorCenterApplication {

  public static void main(String[] args) {
    int ioWorkerCount = Runtime.getRuntime().availableProcessors() << 1;
    System.setProperty("reactor.netty.ioWorkerCount", String.valueOf(ioWorkerCount));
    SpringApplication.run(MonitorCenterApplication.class, args);
  }
}

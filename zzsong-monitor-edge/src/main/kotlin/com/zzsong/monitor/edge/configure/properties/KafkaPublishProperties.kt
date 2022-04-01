package com.zzsong.monitor.edge.configure.properties

/**
 * kafka发布配置
 *
 * @author 宋志宗 on 2022/3/19
 */
class KafkaPublishProperties {
  /** 机器地址 */
  var brokers = "127.0.0.1:9092"

  /** 时序数据写入的topic */
  var timeSeriesTopic = "zzsong_dev_monitor_time_series"
}

package com.zzsong.monitor.edge.infrastructure.output.kafka

/**
 * kafka发布配置
 *
 * @author 宋志宗 on 2022/3/19
 */
class KafkaOutputProperties {
  /** 机器地址 */
  var brokers = ""

  /** 时序数据写入的topic */
  var topic = ""
}

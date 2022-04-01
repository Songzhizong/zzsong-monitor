package com.zzsong.monitor.common.pojo;

import com.zzsong.monitor.common.constants.ConnectType;
import lombok.Getter;
import lombok.Setter;

/**
 * 集群信息
 *
 * @author 宋志宗 on 2022/3/19
 */
@Getter
@Setter
public class Cluster {
  /** 主键 */
  private long id;

  /** 集群编码 */
  private String code;

  /** 集群备注 */
  private String note;

  /** 访问地址, 直连型必填 */
  private String address;

  /** 连接类型 */
  private ConnectType connectType;

  /** 创建时间 */
  private long createdTime;

  /** 更新时间 */
  private long updatedTime;
}

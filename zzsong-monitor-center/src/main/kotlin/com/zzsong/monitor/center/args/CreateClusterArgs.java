package com.zzsong.monitor.center.args;

import com.zzsong.monitor.common.constants.ConnectType;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * 创建集群参数
 *
 * @author 宋志宗 on 2022/3/19
 */
@Getter
@Setter
public class CreateClusterArgs {
  /**
   * 集群编码
   *
   * @required
   */
  @Nullable
  private String code;
  @Nullable
  private String note;
  @Nullable
  private String address;
  /**
   * 连接方式
   *
   * @required
   */
  @Nullable
  private ConnectType connectType;
}

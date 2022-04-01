package com.zzsong.monitor.common.dto.req;

import com.zzsong.monitor.common.constants.ConnectType;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * 更新集群信息参数
 *
 * @author 宋志宗 on 2022/3/19
 */
@Getter
@Setter
public class UpdateClusterArgs {
  /** 备注 */
  @Nullable
  private String note;

  /** 地址 */
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

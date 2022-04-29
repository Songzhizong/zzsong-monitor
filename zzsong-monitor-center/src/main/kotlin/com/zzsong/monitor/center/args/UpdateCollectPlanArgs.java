package com.zzsong.monitor.center.args;

import com.zzsong.monitor.common.pojo.PromCollectCfg;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2022/4/24
 */
@Getter
@Setter
public class UpdateCollectPlanArgs {

  /**
   * 采集计划名称
   *
   * @required
   */
  @Nullable
  private String name;

  /** 备注 */
  @Nullable
  private String note;

  /** prometheus采集配置, 采集类型为PROMETHEUS必填 */
  @Nullable
  private PromCollectCfg prometheus;
}

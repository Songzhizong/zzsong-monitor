package com.zzsong.monitor.center.args;

import com.zzsong.monitor.common.constants.CollectType;
import com.zzsong.monitor.common.pojo.PromCollectCfg;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * 创建采集计划参数
 *
 * @author 宋志宗 on 2022/4/22
 */
@Getter
@Setter
public class CreateCollectPlanArgs {

  /** 业务组id */
  @Nullable
  private Long bizGroupId;

  /**
   * 集群编码
   *
   * @required
   */
  @Nullable
  private String cluster;

  /** 资源标识 */
  @Nullable
  private String ident;

  /**
   * 采集计划名称
   *
   * @required
   */
  @Nullable
  private String name;

  /**
   * 采集类型
   *
   * @required
   */
  @Nullable
  private CollectType type;

  /** prometheus采集配置, 采集类型为PROMETHEUS必填 */
  @Nullable
  private PromCollectCfg prometheus;

  /** 备注 */
  @Nullable
  private String note;
}

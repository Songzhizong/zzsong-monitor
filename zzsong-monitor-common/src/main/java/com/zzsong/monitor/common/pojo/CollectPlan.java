package com.zzsong.monitor.common.pojo;

import com.zzsong.monitor.common.constants.CollectType;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2022/4/22
 */
@Getter
@Setter
public class CollectPlan {
  /** 最低执行频率 */
  private static final long MIN_FREQUENCY = 5;

  /** 主键 */
  private long id;

  /** 业务组id */
  private long bizGroupId;

  /** 集群编码 */
  @Nonnull
  private String cluster;

  /** 资源标识 */
  @Nullable
  private String ident;

  /** 名称 */
  @Nonnull
  private String name;

  /** 采集类型 */
  @Nonnull
  private CollectType type;

  /** prometheus采集配置, 采集类型为PROMETHEUS必填 */
  @Nullable
  private PromCollectCfg prometheus;

  /** 备注 */
  @Nullable
  private String note;

  /** 是否为启用状态 */
  private boolean enabled;

  /** 是否已被删除 */
  private boolean deleted = false;

  /** 创建时间 */
  private long createdTime;

  /** 更新时间 */
  private long updatedTime;
}

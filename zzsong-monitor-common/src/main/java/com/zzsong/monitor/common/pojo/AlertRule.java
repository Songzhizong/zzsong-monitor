package com.zzsong.monitor.common.pojo;

import com.zzsong.monitor.common.constants.AlertRuleType;
import com.zzsong.monitor.common.constants.NotifyChannel;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * 告警规则
 *
 * @author 宋志宗 on 2022/4/25
 */
@Getter
@Setter
public class AlertRule {

  /** 主键 */
  private long id;

  /** 业务组id */
  private long bizGroupId;

  /** 集群编码 */
  @Nonnull
  private String cluster;

  /** 名称 */
  @Nonnull
  private String name;

  /** 备注 */
  @Nullable
  private String note;

  /** 告警级别 */
  private int level;

  /** 告警附加标签 */
  @Nullable
  private Set<String> appendTags;

  /** 告警回调地址 */
  @Nullable
  private Set<String> callbackUrls;

  /** 通知渠道 */
  @Nullable
  private Set<NotifyChannel> notifyChannels;

  /** 重复通知频率, 单位分钟 */
  private int notifyRepeatStep;

  /** 留观时长 */
  private int recoverDuration;

  /** 恢复时是否通知 */
  private boolean notifyRecovered;

  /** 每周中的生效日 1 ~ 7 代表周一到周日 */
  @Nonnull
  private Set<Integer> enableDaysOfWeek;

  /** 生效起始时间 08:00:00 */
  @Nonnull
  private String enableStartTime;

  /** 生效结束时间 23:59:59 */
  @Nonnull
  private String enableEndTime;

  /** 规则类型 */
  @Nonnull
  private AlertRuleType type;

  /** prometheus告警规则配置 */
  @Nullable
  private PromAlertRuleCfg prometheus;

  /** 是否为启用状态 */
  private boolean enabled;

  /** 是否已被删除 */
  private boolean deleted;

  /** 创建时间 */
  private long createdTime;

  /** 更新时间 */
  private long updatedTime;

}

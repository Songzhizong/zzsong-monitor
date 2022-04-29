package com.zzsong.monitor.center.args;

import com.zzsong.monitor.common.constants.AlertRuleType;
import com.zzsong.monitor.common.pojo.PromAlertRuleCfg;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 新建告警规则参数
 *
 * @author 宋志宗 on 2022/4/25
 */
@Getter
@Setter
public class CreateAlertRuleArgs {

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

  /**
   * 名称
   *
   * @required
   */
  @Nullable
  private String name;

  /** 备注 */
  @Nullable
  private String note;

  /** 告警级别 */
  private int level = 2;

  /** 告警附加标签 */
  @Nullable
  private Set<String> appendTags;

  /** 告警回调地址 */
  @Nullable
  private Set<String> callbackUrls;

  /** 通知渠道 */
  @Nullable
  private Set<String> notifyChannels;

  /** 重复通知频率, 单位分钟 */
  private int notifyRepeatStep = 0;

  /** 留观时长 */
  private int recoverDuration = 0;

  /** 恢复时是否通知 */
  private boolean notifyRecovered = true;

  /**
   * 每周中的生效日 1 ~ 7 代表周一到周日
   *
   * @required
   */
  @Nullable
  private Set<Integer> enableDaysOfWeek = new LinkedHashSet<>();

  /**
   * 生效起始时间 08:00:00
   *
   * @required
   */
  @Nullable
  private String enableStartTime;

  /**
   * 生效结束时间 23:59:59
   *
   * @required
   */
  @Nullable
  private String enableEndTime;

  /**
   * 规则类型
   *
   * @required
   */
  @Nullable
  private AlertRuleType type;

  /** prometheus告警规则配置 */
  @Nullable
  private PromAlertRuleCfg prometheus;

  /** 是否启用 */
  private boolean enabled = true;
}

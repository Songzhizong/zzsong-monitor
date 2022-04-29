package com.zzsong.monitor.center.args;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * 选择性更新告警规则字段请求参数
 *
 * @author 宋志宗 on 2022/4/25
 */
@Getter
@Setter
public class SelectivityUpdateAlertRuleArgs {

  /** 集群编码 */
  @Nullable
  private String cluster;

  /** 备注 */
  @Nullable
  private String note;

  /** 告警级别 */
  private Integer level;

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
  @Nullable
  private Integer notifyRepeatStep;

  /** 留观时长 */
  @Nullable
  private Integer recoverDuration;

  /** 恢复时是否通知 */
  private Boolean notifyRecovered;

  /** 每周中的生效日 1 ~ 7 代表周一到周日 */
  @Nullable
  private Set<Integer> enableDaysOfWeek;

  /** 生效起始时间 08:00:00 */
  @Nullable
  private String enableStartTime;

  /** 生效结束时间 23:59:59 */
  @Nullable
  private String enableEndTime;

  /** 是否启用 */
  @Nullable
  private Boolean enabled;

  public boolean isAllNull() {
    return cluster == null
      && note == null
      && level == null
      && appendTags == null
      && callbackUrls == null
      && notifyChannels == null
      && notifyRepeatStep == null
      && recoverDuration == null
      && notifyRecovered == null
      && enableDaysOfWeek == null
      && enableStartTime == null
      && enableEndTime == null
      && enabled == null;
  }
}

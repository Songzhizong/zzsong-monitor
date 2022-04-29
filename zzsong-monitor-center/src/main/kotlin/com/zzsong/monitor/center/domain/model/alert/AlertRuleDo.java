package com.zzsong.monitor.center.domain.model.alert;

import cn.idealframework.date.DateTimeFormatters;
import cn.idealframework.date.DateTimes;
import cn.idealframework.lang.Sets;
import cn.idealframework.lang.StringUtils;
import cn.idealframework.transmission.exception.BadRequestException;
import cn.idealframework.util.Asserts;
import com.zzsong.monitor.center.args.CreateAlertRuleArgs;
import com.zzsong.monitor.common.constants.AlertRuleType;
import com.zzsong.monitor.common.constants.NotifyChannel;
import com.zzsong.monitor.common.pojo.AlertRule;
import com.zzsong.monitor.common.pojo.PromAlertRuleCfg;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 告警规则
 *
 * @author 宋志宗 on 2022/4/24
 */
@Slf4j
@Getter
@Setter
@Document(AlertRuleDo.DOCUMENT_NAME)
@CompoundIndexes({
  @CompoundIndex(name = "bizGroupId", def = "{bizGroupId:1}"),
  @CompoundIndex(name = "cluster", def = "{cluster:1}"),
})
public class AlertRuleDo {
  public static final String DOCUMENT_NAME = "monitor_alert_rule";

  public static final int MAX_NANO_OF_SECOND = 999999999;

  /** 主键 */
  @Id
  private long id = -1;

  /** 业务组id */
  private long bizGroupId = -1;

  /** 集群编码 */
  @Nonnull
  private String cluster = "";

  /** 名称 */
  @Nonnull
  private String name = "";

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
  private int notifyRepeatStep = 0;

  /** 留观时长 */
  private int recoverDuration = 0;

  /** 恢复时是否通知 */
  private boolean notifyRecovered = true;

  /** 每周中的生效日 1 ~ 7 代表周一到周日 */
  @Nonnull
  private Set<Integer> enableDaysOfWeek = new LinkedHashSet<>();

  /** 生效起始时间 08:00:00 */
  @Nonnull
  private String enableStartTime;

  /** 生效结束时间 23:59:59 */
  @Nonnull
  private String enableEndTime;

  /** 规则类型 */
  @Nonnull
  private AlertRuleType type = AlertRuleType.PROMETHEUS;

  /** prometheus告警规则配置 */
  @Nullable
  private PromAlertRuleCfg prometheus;

  /** 是否为启用状态 */
  private boolean enabled = true;

  /** 是否已被删除 */
  private boolean deleted = false;

  /** 版本号 */
  @Version
  private long version = 0;

  /** 创建时间 */
  @CreatedDate
  private long createdTime;

  /** 更新时间 */
  @LastModifiedDate
  private long updatedTime;

  public static void checkLevel(int level) {
    Asserts.assertTrue(level > 0 && level < 4, "非法的告警等级");
  }

  public static void checkTags(@Nullable Set<String> appendTags) {
    if (Sets.isEmpty(appendTags)) {
      return;
    }
    for (String appendTag : appendTags) {
      String[] split = StringUtils.split(appendTag, "=");
      Asserts.assertTrue(split.length == 2, "非法的附加标签: " + appendTag);
    }
  }

  @Nonnull
  public static Set<Integer> checkEnableDaysOfWeek(@Nullable Set<Integer> enableDaysOfWeek) {
    Asserts.notEmpty(enableDaysOfWeek, "未配置生效日");
    for (Integer integer : enableDaysOfWeek) {
      Asserts.assertTrue(integer > 0 && integer < 8, "非法的生效日: " + integer);
    }
    return enableDaysOfWeek;
  }

  public static void checkLocalTime(@Nonnull String time) {
    String pattern = DateTimes.HH_MM_SS;
    try {
      LocalTime.parse(time, DateTimeFormatters.getFormatter(pattern));
    } catch (Exception e) {
      String message = "时间格式非法: " + time + " , 应为: " + pattern;
      log.info(message);
      throw new BadRequestException(message);
    }
  }

  @Nonnull
  public static AlertRuleDo create(@Nonnull CreateAlertRuleArgs args) {
    AlertRuleDo alertRuleDo = new AlertRuleDo();
    Long bizGroupId = args.getBizGroupId();
    if (bizGroupId != null && bizGroupId > 1) {
      alertRuleDo.setBizGroupId(bizGroupId);
    }
    String cluster = args.getCluster();
    Asserts.notBlank(cluster, "cluster为空");
    alertRuleDo.setCluster(cluster);
    String name = args.getName();
    Asserts.notBlank(name, "name为空");
    alertRuleDo.setName(name);
    alertRuleDo.setNote(args.getNote());
    int level = args.getLevel();
    checkLevel(level);
    alertRuleDo.setLevel(level);
    Set<String> appendTags = args.getAppendTags();
    checkTags(appendTags);
    alertRuleDo.setAppendTags(appendTags);
    alertRuleDo.setCallbackUrls(args.getCallbackUrls());
    Set<String> channels = args.getNotifyChannels();
    alertRuleDo.setChannels(channels);
    alertRuleDo.setNotifyRepeatStep(args.getNotifyRepeatStep());
    alertRuleDo.setRecoverDuration(args.getRecoverDuration());
    alertRuleDo.setNotifyRecovered(args.getNotifyRecovered());
    alertRuleDo.setEnableDaysOfWeek(args.getEnableDaysOfWeek());
    alertRuleDo.setEnableStartTime(args.getEnableStartTime());
    alertRuleDo.setEnableEndTime(args.getEnableEndTime());
    AlertRuleType type = args.getType();
    Asserts.nonnull(type, "type为空");
    alertRuleDo.setType(type);
    if (type == AlertRuleType.PROMETHEUS) {
      PromAlertRuleCfg prometheus = args.getPrometheus();
      Asserts.nonnull(prometheus, "prometheus规则配置为空");
      prometheus.check();
      alertRuleDo.setPrometheus(prometheus);
    }
    alertRuleDo.setEnabled(args.getEnabled());
    return alertRuleDo;
  }

  @Nonnull
  public AlertRule toAlertRule() {
    AlertRule alertRule = new AlertRule();
    alertRule.setId(this.getId());
    alertRule.setBizGroupId(this.getBizGroupId());
    alertRule.setCluster(this.getCluster());
    alertRule.setName(this.getName());
    alertRule.setNote(this.getNote());
    alertRule.setLevel(this.getLevel());
    alertRule.setAppendTags(this.getAppendTags());
    alertRule.setCallbackUrls(this.getCallbackUrls());
    alertRule.setNotifyChannels(this.getNotifyChannels());
    alertRule.setNotifyRepeatStep(this.getNotifyRepeatStep());
    alertRule.setRecoverDuration(this.getRecoverDuration());
    alertRule.setNotifyRecovered(this.getNotifyRecovered());
    alertRule.setEnableDaysOfWeek(this.getEnableDaysOfWeek());
    alertRule.setEnableStartTime(this.getEnableStartTime());
    alertRule.setEnableEndTime(this.getEnableEndTime());
    alertRule.setType(this.getType());
    alertRule.setPrometheus(this.getPrometheus());
    alertRule.setEnabled(this.getEnabled());
    alertRule.setDeleted(this.getDeleted());
    alertRule.setCreatedTime(this.getCreatedTime());
    alertRule.setUpdatedTime(this.getUpdatedTime());
    return alertRule;
  }

  private void setChannels(@Nullable Set<String> channels) {
    if (Sets.isEmpty(channels)) {
      this.setNotifyChannels(null);
      return;
    }
    Set<NotifyChannel> notifyChannels = new LinkedHashSet<>();
    for (String channel : channels) {
      NotifyChannel notifyChannel = NotifyChannel.ofName(channel);
      if (notifyChannel != null) {
        notifyChannels.add(notifyChannel);
      }
    }
    this.setNotifyChannels(notifyChannels);
  }

  public void setEnableDaysOfWeek(@Nullable Set<Integer> enableDaysOfWeek) {
    checkEnableDaysOfWeek(enableDaysOfWeek);
    this.enableDaysOfWeek = enableDaysOfWeek;
  }

  public void setEnableStartTime(@Nullable String enableStartTime) {
    Asserts.notBlank(enableStartTime, "生效起始时间为空");
    checkLocalTime(enableStartTime);
    this.enableStartTime = enableStartTime;
  }

  public void setEnableEndTime(@Nullable String enableEndTime) {
    Asserts.notBlank(enableEndTime, "生效结束时间为空");
    checkLocalTime(enableEndTime);
    this.enableEndTime = enableEndTime;
  }
}

package com.zzsong.monitor.common.constants;

import cn.idealframework.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 通知渠道
 *
 * @author 宋志宗 on 2022/4/25
 */
public enum NotifyChannel {
  /** 邮箱通知 */
  EMAIL,
  /** 短信通知 */
  SMS,
  ;

  @Nullable
  public static NotifyChannel ofName(@Nonnull String name) {
    if (StringUtils.isBlank(name)) {
      return null;
    }
    String upperCase = name.toUpperCase();
    switch (upperCase) {
      case "EMAIL":
        return EMAIL;
      case "SMS":
        return SMS;
      default:
        return null;
    }
  }
}

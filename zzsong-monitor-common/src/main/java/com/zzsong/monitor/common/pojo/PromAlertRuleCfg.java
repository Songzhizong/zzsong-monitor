package com.zzsong.monitor.common.pojo;

import cn.idealframework.util.Asserts;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * prometheus告警规则配置信息
 *
 * @author 宋志宗 on 2022/4/24
 */
@Getter
@Setter
public class PromAlertRuleCfg {

  /** promql */
  @Nonnull
  private String promQl = "";

  /** 执行间隔, 单位秒 */
  private int promEvalInterval = 15;

  /** 持续时间, 单位秒 */
  private int promForDuration = 60;

  public void check() {
    Asserts.notBlank(promQl, "promQl不能为空");
    Asserts.assertTrue(promEvalInterval > 0, "执行间隔必须大于0");
  }
}

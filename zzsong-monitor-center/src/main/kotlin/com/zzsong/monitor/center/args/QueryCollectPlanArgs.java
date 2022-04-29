package com.zzsong.monitor.center.args;

import cn.idealframework.transmission.Paging;
import lombok.Getter;
import lombok.Setter;

/**
 * 查询采集计划参数
 *
 * @author 宋志宗 on 2022/4/22
 */
@Getter
@Setter
public class QueryCollectPlanArgs {
  /** 分页参数 */
  private Paging paging = Paging.of(1, 10);

}

package com.zzsong.monitor.center.domain.model.staff;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

/**
 * 业务组实体
 *
 * @author 宋志宗 on 2022/3/19
 */
@Getter
@Setter
public class BizGroupDo {

  /** 主键 */
  @Id
  private Long id;
}

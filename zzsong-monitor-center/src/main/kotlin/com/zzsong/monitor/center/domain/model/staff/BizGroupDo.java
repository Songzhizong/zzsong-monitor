package com.zzsong.monitor.center.domain.model.staff;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 业务组实体
 *
 * @author 宋志宗 on 2022/3/19
 */
@Getter
@Setter
@Document(BizGroupDo.DOCUMENT_NAME)
@CompoundIndexes({
  @CompoundIndex(name = "name", def = "{name:1}", unique = true),
})
public class BizGroupDo {
  public static final String DOCUMENT_NAME = "monitor_biz_group";

  /** 主键 */
  @Id
  private Long id;

  /** 业务组名称 */
  @Nonnull
  private String name;

  /** 备注 */
  @Nullable
  private String note;

  /** 版本号 */
  @Version
  private long version = 0;

  /** 创建时间 */
  @CreatedDate
  private long createdTime;

  /** 更新时间 */
  @LastModifiedDate
  private long updatedTime;
}

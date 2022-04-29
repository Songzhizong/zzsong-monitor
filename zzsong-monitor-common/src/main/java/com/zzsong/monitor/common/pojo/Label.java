package com.zzsong.monitor.common.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author 宋志宗 on 2022/4/23
 */

@Getter
@Setter
@Accessors(chain = true)
public class Label {
  /** 名称 */
  @Nonnull
  private String name;

  /** 值 */
  @Nonnull
  private String value;

  @Nonnull
  public static Label of(@Nonnull String name, @Nonnull String value) {
    return new Label().setName(name).setValue(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Label label = (Label) o;
    return name.equals(label.name) && value.equals(label.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }
}

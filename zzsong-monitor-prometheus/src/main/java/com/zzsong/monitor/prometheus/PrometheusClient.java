package com.zzsong.monitor.prometheus;

import com.zzsong.monitor.prometheus.data.Metadata;
import com.zzsong.monitor.prometheus.data.PrometheusResult;
import com.zzsong.monitor.prometheus.data.QueryResp;
import com.zzsong.monitor.prometheus.impl.PrometheusClientImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 同步prometheus客户端
 *
 * @author 宋志宗 on 2022/4/19
 */
public interface PrometheusClient {

  @Nonnull
  static PrometheusClient newInstance(@Nonnull String baseReadUrl,
                                      @Nonnull Duration timeout) {
    return new PrometheusClientImpl(baseReadUrl, timeout);
  }

  /**
   * 获取元数据
   *
   * @author 宋志宗 on 2022/4/19
   */
  @Nonnull
  PrometheusResult<Map<String, List<Metadata>>> metadata();

  /**
   * 获取标签
   *
   * @param start 起始时间戳 浮点型 单位秒
   * @param end   结束时间戳 浮点型 单位秒
   * @param match 匹配条件
   * @author 宋志宗 on 2022/4/20
   */
  @Nonnull
  PrometheusResult<List<String>> getLabels(@Nullable Double start,
                                           @Nullable Double end,
                                           @Nullable String match);

  /**
   * 获取指定标签的值
   *
   * @param label 标签名称
   * @param start 起始时间戳 浮点型 单位秒
   * @param end   结束时间戳 浮点型 单位秒
   * @param match 匹配条件
   * @author 宋志宗 on 2022/4/19
   */
  @Nonnull
  PrometheusResult<List<String>> labelValues(@Nonnull String label,
                                             @Nullable Double start,
                                             @Nullable Double end,
                                             @Nullable String match);

  /**
   * @param start 起始时间 TZ格式 2022-04-19T15:25:30.711Z
   * @param end   结束时间 TZ格式 2022-04-20T03:25:30.711Z
   * @param match 匹配条件
   * @author 宋志宗 on 2022/4/19
   */
  @Nonnull
  PrometheusResult<List<Map<String, String>>> series(@Nullable String start,
                                                     @Nullable String end,
                                                     @Nonnull String match);

  /**
   * 指定时间查询指标数据
   *
   * @param query promQL
   * @param time  秒级时间戳
   * @author 宋志宗 on 2022/4/19
   */
  @Nonnull
  PrometheusResult<QueryResp> query(@Nonnull String query, @Nullable Double time);

  /**
   * 按时间区间查询数据
   *
   * @param query promQL
   * @param start 起始时间戳 浮点型 单位秒
   * @param end   结束时间戳 浮点型 单位秒
   * @param step  步长,单位秒
   * @author 宋志宗 on 2022/4/19
   */
  @Nonnull
  PrometheusResult<QueryResp> queryRange(@Nonnull String query,
                                         double start,
                                         double end,
                                         int step);

}

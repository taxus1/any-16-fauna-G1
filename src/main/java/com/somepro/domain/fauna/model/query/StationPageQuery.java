package com.somepro.domain.fauna.model.query;

import com.somepro.domain.fauna.model.MonitorStation;
import com.somepro.domain.shared.support.DomainChecks;

/**
 * 监测站分页查询条件（领域值对象）。
 *
 * 「一个条件都不填」时各字段为 null，仓储不加任何过滤，把整份名册按页调出来。
 * status 已解析成枚举，非法取值在应用层就被挡成业务异常，不会拼进 SQL。
 */
public record StationPageQuery(String stationNo, String name, String region,
                               MonitorStation.Level level, MonitorStation.Status status) {

    public static StationPageQuery of(String stationNo, String name, String region,
                                      String level, String status) {
        return new StationPageQuery(
                nullIfBlank(stationNo),
                nullIfBlank(name),
                nullIfBlank(region),
                level == null || level.isBlank()
                        ? null : DomainChecks.parseEnum(MonitorStation.Level.class, level, "层级"),
                status == null || status.isBlank()
                        ? null : DomainChecks.parseEnum(MonitorStation.Status.class, status, "状态"));
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}

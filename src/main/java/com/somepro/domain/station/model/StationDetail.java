package com.somepro.domain.station.model;

/**
 * 监测站详情（领域值对象）：监测站本体 + 名下监测点统计。
 *
 * 在册/停测数量由应用层向监测点仓储查询后组装，与点位模块读的是同一份账，
 * 保证详情里的数字和点位名单对得上。
 */
public record StationDetail(MonitorStation station, long activeSiteCount, long inactiveSiteCount) {
}

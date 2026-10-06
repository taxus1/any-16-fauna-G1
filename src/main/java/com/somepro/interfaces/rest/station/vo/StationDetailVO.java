package com.somepro.interfaces.rest.station.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 监测站详情对外返回对象（VO，用户接口层）。
 *
 * 比列表行多带名下监测点统计：activeSiteCount 在册数 / inactiveSiteCount 停测数，
 * 与点位模块读同一份账，两边数字对得上。
 */
public record StationDetailVO(Long id, String stationNo, String name, String level, String region,
                              String leader, String phone, String status,
                              long activeSiteCount, long inactiveSiteCount,
                              LocalDateTime createTime) implements Serializable {
}

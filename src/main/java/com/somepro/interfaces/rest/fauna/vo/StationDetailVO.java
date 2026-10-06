package com.somepro.interfaces.rest.fauna.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 监测站详情 VO：档案 + 名下点位数（在册 / 停测）。
 * 两个计数由站点表实时 COUNT，与监测点模块同口径。
 */
public record StationDetailVO(StationVO station, long activeSiteCount, long inactiveSiteCount)
        implements Serializable {
}

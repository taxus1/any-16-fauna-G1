package com.somepro.interfaces.rest.fauna.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 监测点对外 VO（不可变 record）。每行带编号 siteNo。
 */
public record SiteVO(Long id, String siteNo, Long stationId, String siteType, String habitat,
                     String location, String status, LocalDateTime createTime)
        implements Serializable {
}

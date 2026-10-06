package com.somepro.domain.fauna.model.query;

import com.somepro.domain.fauna.model.MonitorSite;
import com.somepro.domain.shared.support.DomainChecks;

/**
 * 监测点分页查询条件（领域值对象）。
 *
 * 可按所属站、编号、类型、生境、状态过滤；全部缺省即翻整份点位名册。
 */
public record SitePageQuery(Long stationId, String siteNo, MonitorSite.SiteType siteType,
                            MonitorSite.Habitat habitat, MonitorSite.Status status) {

    public static SitePageQuery of(Long stationId, String siteNo, String siteType,
                                   String habitat, String status) {
        return new SitePageQuery(
                stationId,
                siteNo == null || siteNo.isBlank() ? null : siteNo.trim(),
                siteType == null || siteType.isBlank()
                        ? null : DomainChecks.parseEnum(MonitorSite.SiteType.class, siteType, "点位类型"),
                habitat == null || habitat.isBlank()
                        ? null : DomainChecks.parseEnum(MonitorSite.Habitat.class, habitat, "生境"),
                status == null || status.isBlank()
                        ? null : DomainChecks.parseEnum(MonitorSite.Status.class, status, "状态"));
    }
}

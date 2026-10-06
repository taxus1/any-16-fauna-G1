package com.somepro.infrastructure.persistence.fauna.converter;

import com.somepro.domain.fauna.model.MonitorSite;
import com.somepro.infrastructure.persistence.fauna.po.MonitorSitePO;

/**
 * MonitorSitePO（表）↔ MonitorSite（领域）唯一转换入口（基础设施层）。
 */
public final class MonitorSitePoConverter {

    private MonitorSitePoConverter() {
    }

    public static MonitorSitePO toPo(MonitorSite d) {
        MonitorSitePO po = new MonitorSitePO();
        po.setId(d.getId());
        po.setSiteNo(d.getSiteNo());
        po.setStationId(d.getStationId());
        po.setSiteType(d.getSiteType() == null ? null : d.getSiteType().name());
        po.setHabitat(d.getHabitat() == null ? null : d.getHabitat().name());
        po.setLocation(d.getLocation());
        po.setStatus(d.getStatus() == null ? null : d.getStatus().name());
        MonitorStationPoConverter.fillAudit(d, po);
        return po;
    }

    public static MonitorSite toDomain(MonitorSitePO po) {
        MonitorSite d = new MonitorSite();
        d.setId(po.getId());
        d.setSiteNo(po.getSiteNo());
        d.setStationId(po.getStationId());
        d.setSiteType(po.getSiteType() == null ? null : MonitorSite.SiteType.valueOf(po.getSiteType()));
        d.setHabitat(po.getHabitat() == null ? null : MonitorSite.Habitat.valueOf(po.getHabitat()));
        d.setLocation(po.getLocation());
        d.setStatus(po.getStatus() == null ? null : MonitorSite.Status.valueOf(po.getStatus()));
        MonitorStationPoConverter.readAudit(po, d);
        return d;
    }
}

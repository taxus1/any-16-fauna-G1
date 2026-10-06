package com.somepro.infrastructure.persistence.site.converter;

import com.somepro.domain.site.model.MonitorSite;
import com.somepro.infrastructure.persistence.site.po.MonitorSitePO;

/**
 * MonitorSitePO（表）↔ MonitorSite（领域）转换器（基础设施层）。
 */
public final class MonitorSitePoConverter {

    private MonitorSitePoConverter() {
    }

    public static MonitorSitePO toPo(MonitorSite domain) {
        MonitorSitePO po = new MonitorSitePO();
        po.setId(domain.getId());
        po.setSiteNo(domain.getSiteNo());
        po.setStationId(domain.getStationId());
        po.setSiteType(domain.getSiteType());
        po.setHabitat(domain.getHabitat());
        po.setLocation(domain.getLocation());
        po.setStatus(domain.getStatus());
        po.setDelFlag(domain.getDelFlag());
        po.setCreateBy(domain.getCreateBy());
        po.setCreateTime(domain.getCreateTime());
        po.setUpdateBy(domain.getUpdateBy());
        po.setUpdateTime(domain.getUpdateTime());
        return po;
    }

    public static MonitorSite toDomain(MonitorSitePO po) {
        MonitorSite domain = new MonitorSite();
        domain.setId(po.getId());
        domain.setSiteNo(po.getSiteNo());
        domain.setStationId(po.getStationId());
        domain.setSiteType(po.getSiteType());
        domain.setHabitat(po.getHabitat());
        domain.setLocation(po.getLocation());
        domain.setStatus(po.getStatus());
        domain.setDelFlag(po.getDelFlag());
        domain.setCreateBy(po.getCreateBy());
        domain.setCreateTime(po.getCreateTime());
        domain.setUpdateBy(po.getUpdateBy());
        domain.setUpdateTime(po.getUpdateTime());
        return domain;
    }
}

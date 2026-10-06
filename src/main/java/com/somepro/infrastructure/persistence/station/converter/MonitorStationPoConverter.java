package com.somepro.infrastructure.persistence.station.converter;

import com.somepro.domain.station.model.MonitorStation;
import com.somepro.infrastructure.persistence.station.po.MonitorStationPO;

/**
 * MonitorStationPO（表）↔ MonitorStation（领域）转换器（基础设施层）。
 *
 * 审计字段与 delFlag 一并搬运：新建时为 null 由 MetaObjectHandler 落库填充，
 * 更新时把已有值带过去，配合 @TableLogic 保证只改未删除的行。
 */
public final class MonitorStationPoConverter {

    private MonitorStationPoConverter() {
    }

    public static MonitorStationPO toPo(MonitorStation domain) {
        MonitorStationPO po = new MonitorStationPO();
        po.setId(domain.getId());
        po.setStationNo(domain.getStationNo());
        po.setName(domain.getName());
        po.setLevel(domain.getLevel());
        po.setRegion(domain.getRegion());
        po.setLeader(domain.getLeader());
        po.setPhone(domain.getPhone());
        po.setStatus(domain.getStatus());
        po.setDelFlag(domain.getDelFlag());
        po.setCreateBy(domain.getCreateBy());
        po.setCreateTime(domain.getCreateTime());
        po.setUpdateBy(domain.getUpdateBy());
        po.setUpdateTime(domain.getUpdateTime());
        return po;
    }

    public static MonitorStation toDomain(MonitorStationPO po) {
        MonitorStation domain = new MonitorStation();
        domain.setId(po.getId());
        domain.setStationNo(po.getStationNo());
        domain.setName(po.getName());
        domain.setLevel(po.getLevel());
        domain.setRegion(po.getRegion());
        domain.setLeader(po.getLeader());
        domain.setPhone(po.getPhone());
        domain.setStatus(po.getStatus());
        domain.setDelFlag(po.getDelFlag());
        domain.setCreateBy(po.getCreateBy());
        domain.setCreateTime(po.getCreateTime());
        domain.setUpdateBy(po.getUpdateBy());
        domain.setUpdateTime(po.getUpdateTime());
        return domain;
    }
}

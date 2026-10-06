package com.somepro.infrastructure.persistence.fauna.converter;

import com.somepro.domain.fauna.model.MonitorStation;
import com.somepro.infrastructure.persistence.fauna.po.MonitorStationPO;

/**
 * MonitorStationPO（表）↔ MonitorStation（领域）唯一转换入口（基础设施层）。
 * 层级 / 状态以枚举常量名落库（PROVINCIAL / ACTIVE …），与建表注释口径一致。
 */
public final class MonitorStationPoConverter {

    private MonitorStationPoConverter() {
    }

    public static MonitorStationPO toPo(MonitorStation d) {
        MonitorStationPO po = new MonitorStationPO();
        po.setId(d.getId());
        po.setStationNo(d.getStationNo());
        po.setName(d.getName());
        po.setLevel(d.getLevel() == null ? null : d.getLevel().name());
        po.setRegion(d.getRegion());
        po.setLeader(d.getLeader());
        po.setPhone(d.getPhone());
        po.setStatus(d.getStatus() == null ? null : d.getStatus().name());
        fillAudit(d, po);
        return po;
    }

    public static MonitorStation toDomain(MonitorStationPO po) {
        MonitorStation d = new MonitorStation();
        d.setId(po.getId());
        d.setStationNo(po.getStationNo());
        d.setName(po.getName());
        d.setLevel(po.getLevel() == null ? null : MonitorStation.Level.valueOf(po.getLevel()));
        d.setRegion(po.getRegion());
        d.setLeader(po.getLeader());
        d.setPhone(po.getPhone());
        d.setStatus(po.getStatus() == null ? null : MonitorStation.Status.valueOf(po.getStatus()));
        readAudit(po, d);
        return d;
    }

    static void fillAudit(com.somepro.domain.shared.model.BaseEntity d,
                          com.somepro.infrastructure.persistence.base.BasePO po) {
        po.setDelFlag(d.getDelFlag());
        po.setCreateBy(d.getCreateBy());
        po.setCreateTime(d.getCreateTime());
        po.setUpdateBy(d.getUpdateBy());
        po.setUpdateTime(d.getUpdateTime());
    }

    static void readAudit(com.somepro.infrastructure.persistence.base.BasePO po,
                          com.somepro.domain.shared.model.BaseEntity d) {
        d.setDelFlag(po.getDelFlag());
        d.setCreateBy(po.getCreateBy());
        d.setCreateTime(po.getCreateTime());
        d.setUpdateBy(po.getUpdateBy());
        d.setUpdateTime(po.getUpdateTime());
    }
}

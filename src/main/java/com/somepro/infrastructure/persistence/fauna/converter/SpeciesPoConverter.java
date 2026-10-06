package com.somepro.infrastructure.persistence.fauna.converter;

import com.somepro.domain.fauna.model.Species;
import com.somepro.infrastructure.persistence.fauna.po.SpeciesPO;

/**
 * SpeciesPO（表）↔ Species（领域）唯一转换入口（基础设施层）。
 */
public final class SpeciesPoConverter {

    private SpeciesPoConverter() {
    }

    public static SpeciesPO toPo(Species d) {
        SpeciesPO po = new SpeciesPO();
        po.setId(d.getId());
        po.setSpeciesCode(d.getSpeciesCode());
        po.setName(d.getName());
        po.setLatinName(d.getLatinName());
        po.setProtectionLevel(d.getProtectionLevel() == null ? null : d.getProtectionLevel().name());
        po.setStatus(d.getStatus() == null ? null : d.getStatus().name());
        MonitorStationPoConverter.fillAudit(d, po);
        return po;
    }

    public static Species toDomain(SpeciesPO po) {
        Species d = new Species();
        d.setId(po.getId());
        d.setSpeciesCode(po.getSpeciesCode());
        d.setName(po.getName());
        d.setLatinName(po.getLatinName());
        d.setProtectionLevel(po.getProtectionLevel() == null
                ? null : Species.ProtectionLevel.valueOf(po.getProtectionLevel()));
        d.setStatus(po.getStatus() == null ? null : Species.Status.valueOf(po.getStatus()));
        MonitorStationPoConverter.readAudit(po, d);
        return d;
    }
}

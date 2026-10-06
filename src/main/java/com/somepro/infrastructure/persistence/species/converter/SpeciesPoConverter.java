package com.somepro.infrastructure.persistence.species.converter;

import com.somepro.domain.species.model.Species;
import com.somepro.infrastructure.persistence.species.po.SpeciesPO;

/**
 * SpeciesPO（表）↔ Species（领域）转换器（基础设施层）。
 */
public final class SpeciesPoConverter {

    private SpeciesPoConverter() {
    }

    public static SpeciesPO toPo(Species domain) {
        SpeciesPO po = new SpeciesPO();
        po.setId(domain.getId());
        po.setSpeciesCode(domain.getSpeciesCode());
        po.setName(domain.getName());
        po.setLatinName(domain.getLatinName());
        po.setProtectionLevel(domain.getProtectionLevel());
        po.setStatus(domain.getStatus());
        po.setDelFlag(domain.getDelFlag());
        po.setCreateBy(domain.getCreateBy());
        po.setCreateTime(domain.getCreateTime());
        po.setUpdateBy(domain.getUpdateBy());
        po.setUpdateTime(domain.getUpdateTime());
        return po;
    }

    public static Species toDomain(SpeciesPO po) {
        Species domain = new Species();
        domain.setId(po.getId());
        domain.setSpeciesCode(po.getSpeciesCode());
        domain.setName(po.getName());
        domain.setLatinName(po.getLatinName());
        domain.setProtectionLevel(po.getProtectionLevel());
        domain.setStatus(po.getStatus());
        domain.setDelFlag(po.getDelFlag());
        domain.setCreateBy(po.getCreateBy());
        domain.setCreateTime(po.getCreateTime());
        domain.setUpdateBy(po.getUpdateBy());
        domain.setUpdateTime(po.getUpdateTime());
        return domain;
    }
}

package com.somepro.interfaces.rest.species.converter;

import com.somepro.domain.shared.model.PageResult;
import com.somepro.domain.species.model.Species;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.species.vo.SpeciesVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Species（领域）→ SpeciesVO（对外）转换器（用户接口层）。
 */
public final class SpeciesVoConverter {

    private SpeciesVoConverter() {
    }

    public static SpeciesVO toVo(Species species) {
        return new SpeciesVO(species.getId(), species.getSpeciesCode(), species.getName(),
                species.getLatinName(), species.getProtectionLevel(), species.getStatus(),
                species.getCreateTime());
    }

    public static PageVO<SpeciesVO> toPageVo(PageResult<Species> page) {
        List<SpeciesVO> content = page.content().stream()
                .map(SpeciesVoConverter::toVo)
                .collect(Collectors.toList());
        return new PageVO<>(content, page.total(), page.pageNum(), page.pageSize(), page.totalPages());
    }
}

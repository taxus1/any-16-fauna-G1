package com.somepro.interfaces.rest.fauna.converter;

import com.somepro.domain.fauna.model.Species;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.fauna.vo.SpeciesVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Species（领域）→ 对外 VO 转换器（用户接口层）。
 */
public final class SpeciesVoConverter {

    private SpeciesVoConverter() {
    }

    public static SpeciesVO toVo(Species d) {
        return new SpeciesVO(
                d.getId(),
                d.getSpeciesCode(),
                d.getName(),
                d.getLatinName(),
                d.getProtectionLevel() == null ? null : d.getProtectionLevel().name(),
                d.getStatus() == null ? null : d.getStatus().name(),
                d.getCreateTime());
    }

    public static PageVO<SpeciesVO> toPageVo(PageResult<Species> page) {
        List<SpeciesVO> content = page.content().stream()
                .map(SpeciesVoConverter::toVo)
                .collect(Collectors.toList());
        return new PageVO<>(content, page.total(), page.pageNum(), page.pageSize(), page.totalPages());
    }
}

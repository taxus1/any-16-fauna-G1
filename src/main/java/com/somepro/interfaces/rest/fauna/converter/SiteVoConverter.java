package com.somepro.interfaces.rest.fauna.converter;

import com.somepro.domain.fauna.model.MonitorSite;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.fauna.vo.SiteVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MonitorSite（领域）→ 对外 VO 转换器（用户接口层）。
 */
public final class SiteVoConverter {

    private SiteVoConverter() {
    }

    public static SiteVO toVo(MonitorSite d) {
        return new SiteVO(
                d.getId(),
                d.getSiteNo(),
                d.getStationId(),
                d.getSiteType() == null ? null : d.getSiteType().name(),
                d.getHabitat() == null ? null : d.getHabitat().name(),
                d.getLocation(),
                d.getStatus() == null ? null : d.getStatus().name(),
                d.getCreateTime());
    }

    public static PageVO<SiteVO> toPageVo(PageResult<MonitorSite> page) {
        List<SiteVO> content = page.content().stream()
                .map(SiteVoConverter::toVo)
                .collect(Collectors.toList());
        return new PageVO<>(content, page.total(), page.pageNum(), page.pageSize(), page.totalPages());
    }
}

package com.somepro.interfaces.rest.site.converter;

import com.somepro.domain.shared.model.PageResult;
import com.somepro.domain.site.model.MonitorSite;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.site.vo.SiteVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MonitorSite（领域）→ SiteVO（对外）转换器（用户接口层）。
 */
public final class SiteVoConverter {

    private SiteVoConverter() {
    }

    public static SiteVO toVo(MonitorSite site) {
        return new SiteVO(site.getId(), site.getSiteNo(), site.getStationId(), site.getSiteType(),
                site.getHabitat(), site.getLocation(), site.getStatus(), site.getCreateTime());
    }

    public static PageVO<SiteVO> toPageVo(PageResult<MonitorSite> page) {
        List<SiteVO> content = page.content().stream()
                .map(SiteVoConverter::toVo)
                .collect(Collectors.toList());
        return new PageVO<>(content, page.total(), page.pageNum(), page.pageSize(), page.totalPages());
    }
}

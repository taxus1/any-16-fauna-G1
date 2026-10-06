package com.somepro.interfaces.rest.fauna.converter;

import com.somepro.domain.fauna.model.MonitorStation;
import com.somepro.domain.fauna.model.StationDetail;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.fauna.vo.StationDetailVO;
import com.somepro.interfaces.rest.fauna.vo.StationVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MonitorStation（领域）→ 对外 VO 转换器（用户接口层）。
 */
public final class StationVoConverter {

    private StationVoConverter() {
    }

    public static StationVO toVo(MonitorStation d) {
        return new StationVO(
                d.getId(),
                d.getStationNo(),
                d.getName(),
                d.getLevel() == null ? null : d.getLevel().name(),
                d.getRegion(),
                d.getLeader(),
                d.getPhone(),
                d.getStatus() == null ? null : d.getStatus().name(),
                d.getCreateTime());
    }

    public static StationDetailVO toDetailVo(StationDetail detail) {
        return new StationDetailVO(toVo(detail.station()),
                detail.activeSiteCount(), detail.inactiveSiteCount());
    }

    public static PageVO<StationVO> toPageVo(PageResult<MonitorStation> page) {
        List<StationVO> content = page.content().stream()
                .map(StationVoConverter::toVo)
                .collect(Collectors.toList());
        return new PageVO<>(content, page.total(), page.pageNum(), page.pageSize(), page.totalPages());
    }
}

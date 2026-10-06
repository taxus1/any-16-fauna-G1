package com.somepro.interfaces.rest.station.converter;

import com.somepro.domain.shared.model.PageResult;
import com.somepro.domain.station.model.MonitorStation;
import com.somepro.domain.station.model.StationDetail;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.station.vo.StationDetailVO;
import com.somepro.interfaces.rest.station.vo.StationVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MonitorStation（领域）→ 对外 VO 转换器（用户接口层）。
 *
 * Controller 不许直接把领域对象塞进 Result 返回，否则 delFlag / createBy 等内部字段
 * 会被无意识序列化出去。
 */
public final class StationVoConverter {

    private StationVoConverter() {
    }

    public static StationVO toVo(MonitorStation station) {
        return new StationVO(station.getId(), station.getStationNo(), station.getName(),
                station.getLevel(), station.getRegion(), station.getLeader(), station.getPhone(),
                station.getStatus(), station.getCreateTime());
    }

    public static StationDetailVO toDetailVo(StationDetail detail) {
        MonitorStation station = detail.station();
        return new StationDetailVO(station.getId(), station.getStationNo(), station.getName(),
                station.getLevel(), station.getRegion(), station.getLeader(), station.getPhone(),
                station.getStatus(), detail.activeSiteCount(), detail.inactiveSiteCount(),
                station.getCreateTime());
    }

    public static PageVO<StationVO> toPageVo(PageResult<MonitorStation> page) {
        List<StationVO> content = page.content().stream()
                .map(StationVoConverter::toVo)
                .collect(Collectors.toList());
        return new PageVO<>(content, page.total(), page.pageNum(), page.pageSize(), page.totalPages());
    }
}

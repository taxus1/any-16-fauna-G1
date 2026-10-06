package com.somepro.domain.station.repository;

import com.somepro.domain.shared.model.PageResult;
import com.somepro.domain.station.model.MonitorStation;
import reactor.core.publisher.Mono;

/**
 * 监测站聚合的仓储端口：由领域层定义，基础设施层实现（端口-适配器）。
 *
 * create 与 update 分开：create 负责业务编号分配（并发撞号由实现侧重试），update 只改资料。
 */
public interface MonitorStationRepository {

    /** 新建落库；stationNo 为空时由实现侧按 ST-YYYY-NNNN 生成，非空时按指定编号落库（撞号转业务异常）。 */
    Mono<MonitorStation> create(MonitorStation station);

    /** 按 id 更新资料（编号不改）。 */
    Mono<MonitorStation> update(MonitorStation station);

    Mono<MonitorStation> findById(Long id);

    /** 条件分页：条件全空时返回整份名册；pageNum/pageSize 透传给 PageHelper。 */
    Mono<PageResult<MonitorStation>> page(int pageNum, int pageSize,
                                          String name, String level, String region, String status);
}

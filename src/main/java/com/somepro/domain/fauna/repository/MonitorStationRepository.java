package com.somepro.domain.fauna.repository;

import com.somepro.domain.fauna.model.MonitorStation;
import com.somepro.domain.fauna.model.query.StationPageQuery;
import com.somepro.domain.shared.model.PageResult;
import reactor.core.publisher.Mono;

/**
 * 监测站仓储端口（领域层定义，基础设施层实现）。
 */
public interface MonitorStationRepository {

    Mono<MonitorStation> save(MonitorStation station);

    Mono<MonitorStation> findById(Long id);

    /** 按编号查（编号全局唯一），查不到发空信号。 */
    Mono<MonitorStation> findByNo(String stationNo);

    /**
     * 按业务内容查重：同编号之外，同名 + 同辖区 + 同层级的在档站也视为同一份登记，
     * 用来兜住「同一个登记前后脚涌进来两条」。
     */
    Mono<MonitorStation> findDuplicate(String name, String region, MonitorStation.Level level);

    /** 取当前年份段（ST-2026-）下的最大编号；一条都没有时发空信号。 */
    Mono<String> findMaxStationNo();

    Mono<PageResult<MonitorStation>> page(int pageNum, int pageSize, StationPageQuery query);
}

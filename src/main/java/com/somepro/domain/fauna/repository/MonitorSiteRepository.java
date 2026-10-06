package com.somepro.domain.fauna.repository;

import com.somepro.domain.fauna.model.MonitorSite;
import com.somepro.domain.fauna.model.query.SitePageQuery;
import com.somepro.domain.shared.model.PageResult;
import reactor.core.publisher.Mono;

/**
 * 监测点仓储端口（领域层定义，基础设施层实现）。
 */
public interface MonitorSiteRepository {

    Mono<MonitorSite> save(MonitorSite site);

    Mono<MonitorSite> findById(Long id);

    Mono<MonitorSite> findByNo(String siteNo);

    /**
     * 按业务内容查重：同一站下，同类型 + 同生境 + 同位置说明的在册点视为同一份登记
     *（位置说明可空，空与空匹配）。
     */
    Mono<MonitorSite> findDuplicate(Long stationId, MonitorSite.SiteType siteType,
                                    MonitorSite.Habitat habitat, String location);

    /** 取当前年份段（MP-2026-）下的最大编号；一条都没有时发空信号。 */
    Mono<String> findMaxSiteNo();

    /** 数某站名下指定状态的点位数（@TableLogic 自动只数未删除的），站详情与本模块共用同一口径。 */
    Mono<Long> countByStationAndStatus(Long stationId, MonitorSite.Status status);

    Mono<PageResult<MonitorSite>> page(int pageNum, int pageSize, SitePageQuery query);
}

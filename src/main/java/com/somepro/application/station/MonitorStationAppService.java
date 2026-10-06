package com.somepro.application.station;

import com.somepro.common.exception.BizException;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.domain.site.model.MonitorSite;
import com.somepro.domain.site.repository.MonitorSiteRepository;
import com.somepro.domain.station.model.MonitorStation;
import com.somepro.domain.station.model.StationDetail;
import com.somepro.domain.station.repository.MonitorStationRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 监测站应用层：编排监测站用例（新立、改资料、查看、停用、关闭、名册分页）。
 *
 * 出入参都是领域对象，不认识 PO、也不认识 VO。
 * 关闭前的「名下在册点位」校验、详情里的点位统计，都向监测点仓储端口取数，
 * 与点位模块读的是同一份账，两边数字自然对得上。
 */
@Service
public class MonitorStationAppService {

    private final MonitorStationRepository stationRepository;
    private final MonitorSiteRepository siteRepository;

    public MonitorStationAppService(MonitorStationRepository stationRepository,
                                    MonitorSiteRepository siteRepository) {
        this.stationRepository = stationRepository;
        this.siteRepository = siteRepository;
    }

    /** 新立监测站：默认运行状态；stationNo 留空时由仓储层生成。 */
    public Mono<MonitorStation> createStation(String stationNo, String name, String level,
                                              String region, String leader, String phone) {
        MonitorStation station = MonitorStation.create(name, level, region, leader, phone);
        station.setStationNo(normalizeNo(stationNo));
        return stationRepository.create(station);
    }

    public Mono<MonitorStation> updateStation(Long id, String name, String level,
                                              String region, String leader, String phone) {
        return stationRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("监测站不存在")))
                .flatMap(station -> {
                    station.updateProfile(name, level, region, leader, phone);
                    return stationRepository.update(station);
                });
    }

    /** 站详情：连同名下在册/停测点位数一起带出。 */
    public Mono<StationDetail> detail(Long id) {
        return stationRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("监测站不存在")))
                .flatMap(station -> Mono.zip(
                        siteRepository.countByStationIdAndStatus(id, MonitorSite.STATUS_ACTIVE),
                        siteRepository.countByStationIdAndStatus(id, MonitorSite.STATUS_INACTIVE))
                        .map(counts -> new StationDetail(station, counts.getT1(), counts.getT2())));
    }

    public Mono<MonitorStation> suspend(Long id) {
        return stationRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("监测站不存在")))
                .flatMap(station -> {
                    station.suspend();
                    return stationRepository.update(station);
                });
    }

    /** 关闭：先查名下在册点位数，交给领域规则判定能不能关。 */
    public Mono<MonitorStation> close(Long id) {
        return stationRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("监测站不存在")))
                .flatMap(station -> siteRepository
                        .countByStationIdAndStatus(id, MonitorSite.STATUS_ACTIVE)
                        .flatMap(activeCount -> {
                            station.close(activeCount);
                            return stationRepository.update(station);
                        }));
    }

    public Mono<PageResult<MonitorStation>> pageStations(int pageNum, int pageSize,
                                                         String name, String level,
                                                         String region, String status) {
        return stationRepository.page(pageNum, pageSize, name, level, region, status);
    }

    private static String normalizeNo(String stationNo) {
        return (stationNo == null || stationNo.isBlank()) ? null : stationNo.trim();
    }
}

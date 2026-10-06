package com.somepro.application.fauna;

import com.somepro.application.fauna.support.RegisterSerializer;
import com.somepro.common.exception.BizException;
import com.somepro.domain.fauna.model.MonitorStation;
import com.somepro.domain.fauna.model.StationDetail;
import com.somepro.domain.fauna.model.query.StationPageQuery;
import com.somepro.domain.fauna.repository.MonitorSiteRepository;
import com.somepro.domain.fauna.repository.MonitorStationRepository;
import com.somepro.domain.fauna.support.SerialNumbers;
import com.somepro.domain.shared.model.PageResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 监测站应用服务：编排登记 / 改档 / 状态流转 / 查询用例，业务规则在聚合与领域服务里。
 *
 * 编号规则 ST-2026-0001：注册临界区内取当前最大号 + 1；同内容登记查重只落一份。
 */
@Service
public class MonitorStationAppService {

    private static final String REGISTER_LOCK = "fauna:lock:register:station";

    private final MonitorStationRepository stationRepository;
    private final MonitorSiteRepository siteRepository;
    private final RegisterSerializer registerSerializer;

    public MonitorStationAppService(MonitorStationRepository stationRepository,
                                    MonitorSiteRepository siteRepository,
                                    RegisterSerializer registerSerializer) {
        this.stationRepository = stationRepository;
        this.siteRepository = siteRepository;
        this.registerSerializer = registerSerializer;
    }

    /** 登记监测站：默认运行；并发/重复的同一份登记只落一条。 */
    public Mono<MonitorStation> register(String name, String level, String region,
                                         String leader, String phone) {
        return registerSerializer.serialize(REGISTER_LOCK, () -> {
            String normalizedName = normalize(name);
            String normalizedRegion = normalize(region);
            // 先校验枚举，非法层级不进查重
            MonitorStation.Level parsedLevel = parseLevel(level);
            return stationRepository.findDuplicate(normalizedName, normalizedRegion, parsedLevel)
                    .switchIfEmpty(Mono.defer(() ->
                            nextStationNo()
                                    .map(no -> MonitorStation.register(
                                            no, normalizedName, parsedLevel.name(),
                                            normalizedRegion, leader, phone))
                                    .flatMap(stationRepository::save))
                    .cast(MonitorStation.class));
        });
    }

    /** 修改站档案（名称 / 层级 / 辖区 / 负责人 / 电话）。 */
    public Mono<MonitorStation> update(Long id, String name, String level,
                                       String region, String leader, String phone) {
        return requireStation(id).flatMap(station -> {
            station.edit(name, level, region, leader, phone);
            return stationRepository.save(station);
        });
    }

    /** 停用：ACTIVE → SUSPENDED。 */
    public Mono<MonitorStation> suspend(Long id) {
        return changeStatus(id, MonitorStation::suspend);
    }

    /** 恢复运行：SUSPENDED → ACTIVE。 */
    public Mono<MonitorStation> resume(Long id) {
        return changeStatus(id, MonitorStation::resume);
    }

    /**
     * 关闭：名下还挂着在册点位就不许关，等点位都停测或撤了再说。
     */
    public Mono<MonitorStation> close(Long id) {
        return requireStation(id).flatMap(station ->
                siteRepository.countByStationAndStatus(id, com.somepro.domain.fauna.model.MonitorSite.Status.ACTIVE)
                        .flatMap(activeCount -> {
                            if (activeCount > 0) {
                                return Mono.<MonitorStation>error(new BizException(
                                        "名下还有 " + activeCount + " 个在册监测点，不能关闭；请先停测或撤出点位"));
                            }
                            station.close();
                            return stationRepository.save(station);
                        }));
    }

    public Mono<MonitorStation> getById(Long id) {
        return requireStation(id);
    }

    /** 站详情：档案 + 名下在册/停测点位数（与点位模块同口径 COUNT）。 */
    public Mono<StationDetail> getDetail(Long id) {
        return requireStation(id).flatMap(station ->
                Mono.zip(
                        siteRepository.countByStationAndStatus(id, com.somepro.domain.fauna.model.MonitorSite.Status.ACTIVE),
                        siteRepository.countByStationAndStatus(id, com.somepro.domain.fauna.model.MonitorSite.Status.INACTIVE))
                        .map(counts -> new StationDetail(station, counts.getT1(), counts.getT2())));
    }

    public Mono<PageResult<MonitorStation>> page(int pageNum, int pageSize, StationPageQuery query) {
        return stationRepository.page(pageNum, pageSize, query);
    }

    private Mono<MonitorStation> changeStatus(Long id, java.util.function.Consumer<MonitorStation> transition) {
        return requireStation(id).flatMap(station -> {
            transition.accept(station);
            return stationRepository.save(station);
        });
    }

    private Mono<MonitorStation> requireStation(Long id) {
        return stationRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("监测站不存在")));
    }

    /** 临界区内取最大号 + 1，生成 ST-2026-0001 编号。 */
    private Mono<String> nextStationNo() {
        return stationRepository.findMaxStationNo()
                .defaultIfEmpty(SerialNumbers.stationNo(0))
                .map(max -> {
                    long next = parseSeq(max, SerialNumbers.stationPrefix()) + 1;
                    return SerialNumbers.stationNo(next);
                });
    }

    static long parseSeq(String code, String prefix) {
        try {
            return Long.parseLong(code.substring(prefix.length()));
        } catch (NumberFormatException e) {
            // 库里存在不符合当前规则的旧编号时从 1 起，别把整笔登记打挂
            return 0L;
        }
    }

    private static String normalize(String text) {
        return text == null ? null : text.trim();
    }

    private static MonitorStation.Level parseLevel(String level) {
        if (level == null || level.isBlank()) {
            throw new BizException("层级不能为空");
        }
        try {
            return MonitorStation.Level.valueOf(level.trim());
        } catch (IllegalArgumentException e) {
            throw new BizException("层级取值非法：" + level.trim());
        }
    }
}

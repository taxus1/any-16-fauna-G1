package com.somepro.application.fauna;

import com.somepro.application.fauna.support.RegisterSerializer;
import com.somepro.common.exception.BizException;
import com.somepro.domain.fauna.model.MonitorSite;
import com.somepro.domain.fauna.model.MonitorStation;
import com.somepro.domain.fauna.model.query.SitePageQuery;
import com.somepro.domain.fauna.repository.MonitorSiteRepository;
import com.somepro.domain.fauna.repository.MonitorStationRepository;
import com.somepro.domain.fauna.support.SerialNumbers;
import com.somepro.domain.shared.model.PageResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 监测点应用服务。
 *
 * 挂点规则：点位只能挂到「实实在在且没关闭」的站上 —— 站不存在、停用、关闭都不允许。
 * 编号规则 MP-2026-0001，注册临界区内取最大号 + 1；同内容登记只落一份。
 */
@Service
public class MonitorSiteAppService {

    private static final String REGISTER_LOCK = "fauna:lock:register:site";

    private final MonitorSiteRepository siteRepository;
    private final MonitorStationRepository stationRepository;
    private final RegisterSerializer registerSerializer;

    public MonitorSiteAppService(MonitorSiteRepository siteRepository,
                                 MonitorStationRepository stationRepository,
                                 RegisterSerializer registerSerializer) {
        this.siteRepository = siteRepository;
        this.stationRepository = stationRepository;
        this.registerSerializer = registerSerializer;
    }

    /**
     * 登记监测点。先核对归属站：必须存在且为 ACTIVE（停用/关闭/不存在一律拒绝）。
     */
    public Mono<MonitorSite> register(Long stationId, String siteType, String habitat, String location) {
        if (stationId == null) {
            throw new BizException("所属监测站不能为空");
        }
        return requireActiveStation(stationId)
                .flatMap(station -> registerSerializer.serialize(REGISTER_LOCK, () -> {
                    MonitorSite.SiteType parsedType = parseEnum(MonitorSite.SiteType.class, siteType, "点位类型");
                    MonitorSite.Habitat parsedHabitat = parseEnum(MonitorSite.Habitat.class, habitat, "生境");
                    String normalizedLocation = normalize(location);
                    return siteRepository.findDuplicate(stationId, parsedType, parsedHabitat, normalizedLocation)
                            .switchIfEmpty(Mono.defer(() ->
                                    nextSiteNo()
                                            .map(no -> MonitorSite.register(
                                                    no, stationId, parsedType.name(),
                                                    parsedHabitat.name(), normalizedLocation))
                                            .flatMap(siteRepository::save))
                            .cast(MonitorSite.class));
                }));
    }

    /** 修改点位档案（类型 / 生境 / 位置），归属站不允许通过编辑改挂。 */
    public Mono<MonitorSite> update(Long id, String siteType, String habitat, String location) {
        return requireSite(id).flatMap(site -> {
            site.edit(siteType, habitat, location);
            return siteRepository.save(site);
        });
    }

    /** 在册 → 停测。 */
    public Mono<MonitorSite> deactivate(Long id) {
        return changeStatus(id, MonitorSite::deactivate);
    }

    /** 停测 → 恢复在册。 */
    public Mono<MonitorSite> activate(Long id) {
        return changeStatus(id, MonitorSite::activate);
    }

    public Mono<MonitorSite> getById(Long id) {
        return requireSite(id);
    }

    public Mono<PageResult<MonitorSite>> page(int pageNum, int pageSize, SitePageQuery query) {
        return siteRepository.page(pageNum, pageSize, query);
    }

    private Mono<MonitorSite> changeStatus(Long id, java.util.function.Consumer<MonitorSite> transition) {
        return requireSite(id).flatMap(site -> {
            transition.accept(site);
            return siteRepository.save(site);
        });
    }

    private Mono<MonitorSite> requireSite(Long id) {
        return siteRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("监测点不存在")));
    }

    /** 核对归属站：存在且处于运行状态才允许挂点。 */
    private Mono<MonitorStation> requireActiveStation(Long stationId) {
        return stationRepository.findById(stationId)
                .switchIfEmpty(Mono.error(new BizException("所属监测站不存在，不能登记点位")))
                .flatMap(station -> {
                    if (station.getStatus() != MonitorStation.Status.ACTIVE) {
                        return Mono.<MonitorStation>error(new BizException(
                                "监测站当前状态为 " + station.getStatus().name() + "，只有运行中的站才能登记点位"));
                    }
                    return Mono.just(station);
                });
    }

    /** 临界区内取最大号 + 1，生成 MP-2026-0001 编号。 */
    private Mono<String> nextSiteNo() {
        return siteRepository.findMaxSiteNo()
                .defaultIfEmpty(SerialNumbers.siteNo(0))
                .map(max -> {
                    long next = MonitorStationAppService.parseSeq(max, SerialNumbers.sitePrefix()) + 1;
                    return SerialNumbers.siteNo(next);
                });
    }

    private static String normalize(String text) {
        return text == null ? null : text.trim().isEmpty() ? null : text.trim();
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String code, String label) {
        if (code == null || code.isBlank()) {
            throw new BizException(label + "不能为空");
        }
        try {
            return Enum.valueOf(type, code.trim());
        } catch (IllegalArgumentException e) {
            throw new BizException(label + "取值非法：" + code.trim());
        }
    }
}

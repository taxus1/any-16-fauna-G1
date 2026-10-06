package com.somepro.infrastructure.persistence.station;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.somepro.common.exception.BizException;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.domain.station.model.MonitorStation;
import com.somepro.domain.station.repository.MonitorStationRepository;
import com.somepro.infrastructure.config.ReactiveOperatorContext;
import com.somepro.infrastructure.persistence.audit.AuditContextHolder;
import com.somepro.infrastructure.persistence.station.converter.MonitorStationPoConverter;
import com.somepro.infrastructure.persistence.station.po.MonitorStationPO;
import com.somepro.infrastructure.persistence.support.BizNoGenerator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 监测站仓储适配器：用 MyBatis-Plus 实现领域仓储端口（基础设施层）。
 *
 * 约定同 DemoItemRepositoryImpl：所有 DB 调用经 {@link #blocking} 桥接到 boundedElastic；
 * PO 与领域对象在本类里经 {@link MonitorStationPoConverter} 互转，不泄到外层。
 *
 * 编号分配：stationNo 为空时按 ST-YYYY-NNNN 生成（序号取号段内最大值 +1，并发撞号由
 * {@link BizNoGenerator} 重试）；调用方指定编号时直接落库，撞唯一索引转成业务异常，
 * 不把底层 DuplicateKeyException 甩给上层。
 */
@Repository
public class MonitorStationRepositoryImpl implements MonitorStationRepository {

    /** 编号前缀：ST-年-（如 ST-2026-） */
    private static final String NO_PREFIX = "ST-";

    private final MonitorStationMapper stationMapper;

    public MonitorStationRepositoryImpl(MonitorStationMapper stationMapper) {
        this.stationMapper = stationMapper;
    }

    @Override
    public Mono<MonitorStation> create(MonitorStation station) {
        return blocking(() -> {
            if (station.getStationNo() != null && !station.getStationNo().isBlank()) {
                try {
                    return doInsert(station, station.getStationNo().trim());
                } catch (DuplicateKeyException e) {
                    throw new BizException("监测站编号已存在：" + station.getStationNo());
                }
            }
            String prefix = NO_PREFIX + LocalDate.now().getYear() + "-";
            return BizNoGenerator.insertWithRetry(
                    () -> stationMapper.selectMaxSeq(prefix, prefix.length() + 1),
                    prefix,
                    no -> doInsert(station, no));
        });
    }

    @Override
    public Mono<MonitorStation> update(MonitorStation station) {
        return blocking(() -> {
            MonitorStationPO po = MonitorStationPoConverter.toPo(station);
            stationMapper.updateById(po);
            return MonitorStationPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<MonitorStation> findById(Long id) {
        return blocking(() -> {
            MonitorStationPO po = stationMapper.selectById(id);
            return po == null ? null : MonitorStationPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<PageResult<MonitorStation>> page(int pageNum, int pageSize,
                                                 String name, String level, String region, String status) {
        return this.<PageResult<MonitorStation>>blocking(() -> {
            try {
                PageHelper.startPage(pageNum, pageSize);
                LambdaQueryWrapper<MonitorStationPO> wrapper = Wrappers.<MonitorStationPO>lambdaQuery()
                        .like(hasText(name), MonitorStationPO::getName, name)
                        .eq(hasText(level), MonitorStationPO::getLevel, level)
                        .like(hasText(region), MonitorStationPO::getRegion, region)
                        .eq(hasText(status), MonitorStationPO::getStatus, status)
                        .orderByAsc(MonitorStationPO::getId);
                List<MonitorStationPO> rows = stationMapper.selectList(wrapper);
                long total = rows instanceof com.github.pagehelper.Page
                        ? ((com.github.pagehelper.Page<?>) rows).getTotal()
                        : rows.size();
                List<MonitorStation> content = rows.stream()
                        .map(MonitorStationPoConverter::toDomain)
                        .collect(Collectors.toList());
                return new PageResult<>(content, total, pageNum, pageSize);
            } finally {
                // 分页参数靠 ThreadLocal 传递，必须清理，否则污染线程池里的下一次调用
                PageHelper.clearPage();
            }
        });
    }

    /** 落库：雪花 id + 编号，审计字段由 MetaObjectHandler 填充。 */
    private MonitorStation doInsert(MonitorStation station, String stationNo) {
        station.setStationNo(stationNo);
        MonitorStationPO po = MonitorStationPoConverter.toPo(station);
        po.setId(IdUtil.getSnowflakeNextId());
        stationMapper.insert(po);
        return MonitorStationPoConverter.toDomain(po);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 阻塞 DB 调用 → 响应式链路的桥接器：先取 Reactor Context 里的操作人，
     * 再切到 boundedElastic 执行 JDBC，操作人放进 AuditContextHolder 供审计填充。
     */
    private <T> Mono<T> blocking(Supplier<T> supplier) {
        return Mono.deferContextual(ctx -> {
            String operator = ReactiveOperatorContext.getOperator(ctx);
            return Mono.fromCallable(() -> {
                AuditContextHolder.setOperator(operator);
                try {
                    return supplier.get();
                } finally {
                    AuditContextHolder.clear();
                }
            }).subscribeOn(Schedulers.boundedElastic());
        });
    }
}

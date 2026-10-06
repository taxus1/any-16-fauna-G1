package com.somepro.infrastructure.persistence.fauna;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.somepro.common.exception.BizException;
import com.somepro.domain.fauna.model.MonitorStation;
import com.somepro.domain.fauna.model.query.StationPageQuery;
import com.somepro.domain.fauna.repository.MonitorStationRepository;
import com.somepro.domain.fauna.support.SerialNumbers;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.infrastructure.persistence.fauna.converter.MonitorStationPoConverter;
import com.somepro.infrastructure.persistence.fauna.po.MonitorStationPO;
import com.somepro.infrastructure.persistence.support.AbstractBlockingRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 监测站仓储适配器（基础设施层）：MyBatis-Plus 实现领域端口。
 *
 * - ID 雪花、编号由应用层生成；软删除走 @TableLogic，不手写 del_flag；
 * - 分页统一 PageHelper，用完 finally clearPage；
 * - 唯一索引冲突在这里收口成业务异常 —— 正常路径已由注册锁 + 内容查重挡住，
 *   走到这一层只可能是极端并发/脏数据，不把 SQL 异常原文甩给调用方。
 */
@Repository
public class MonitorStationRepositoryImpl extends AbstractBlockingRepository implements MonitorStationRepository {

    private final MonitorStationMapper mapper;

    public MonitorStationRepositoryImpl(MonitorStationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Mono<MonitorStation> save(MonitorStation station) {
        return blocking(() -> {
            MonitorStationPO po = MonitorStationPoConverter.toPo(station);
            try {
                if (po.getId() == null) {
                    po.setId(IdUtil.getSnowflakeNextId());
                    mapper.insert(po);
                } else {
                    mapper.updateById(po);
                }
            } catch (DuplicateKeyException e) {
                throw new BizException("监测站编号或登记内容已存在，请勿重复登记");
            }
            return MonitorStationPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<MonitorStation> findById(Long id) {
        return blocking(() -> {
            MonitorStationPO po = mapper.selectById(id);
            return po == null ? null : MonitorStationPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<MonitorStation> findByNo(String stationNo) {
        return blocking(() -> {
            MonitorStationPO po = mapper.selectOne(Wrappers.<MonitorStationPO>lambdaQuery()
                    .eq(MonitorStationPO::getStationNo, stationNo)
                    .last("LIMIT 1"));
            return po == null ? null : MonitorStationPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<MonitorStation> findDuplicate(String name, String region, MonitorStation.Level level) {
        return blocking(() -> {
            LambdaQueryWrapper<MonitorStationPO> w = Wrappers.<MonitorStationPO>lambdaQuery()
                    .eq(MonitorStationPO::getName, name)
                    .eq(MonitorStationPO::getRegion, region)
                    .eq(MonitorStationPO::getLevel, level.name())
                    .last("LIMIT 1");
            MonitorStationPO po = mapper.selectOne(w);
            return po == null ? null : MonitorStationPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<String> findMaxStationNo() {
        return blocking(() -> mapper.selectMaxStationNo(SerialNumbers.stationPrefix() + "%"));
    }

    @Override
    public Mono<PageResult<MonitorStation>> page(int pageNum, int pageSize, StationPageQuery query) {
        return this.<PageResult<MonitorStation>>blocking(() -> {
            try {
                PageHelper.startPage(pageNum, pageSize);
                LambdaQueryWrapper<MonitorStationPO> w = Wrappers.<MonitorStationPO>lambdaQuery()
                        .eq(query.stationNo() != null, MonitorStationPO::getStationNo, query.stationNo())
                        .like(query.name() != null, MonitorStationPO::getName, query.name())
                        .like(query.region() != null, MonitorStationPO::getRegion, query.region())
                        .eq(query.level() != null, MonitorStationPO::getLevel,
                                query.level() == null ? null : query.level().name())
                        .eq(query.status() != null, MonitorStationPO::getStatus,
                                query.status() == null ? null : query.status().name())
                        .orderByAsc(MonitorStationPO::getId);
                List<MonitorStationPO> rows = mapper.selectList(w);
                long total = rows instanceof com.github.pagehelper.Page
                        ? ((com.github.pagehelper.Page<?>) rows).getTotal()
                        : rows.size();
                List<MonitorStation> content = rows.stream()
                        .map(MonitorStationPoConverter::toDomain)
                        .collect(Collectors.toList());
                return new PageResult<>(content, total, pageNum, pageSize);
            } finally {
                PageHelper.clearPage();
            }
        });
    }
}

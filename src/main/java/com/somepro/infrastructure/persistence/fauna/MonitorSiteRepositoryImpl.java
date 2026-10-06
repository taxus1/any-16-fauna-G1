package com.somepro.infrastructure.persistence.fauna;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.somepro.common.exception.BizException;
import com.somepro.domain.fauna.model.MonitorSite;
import com.somepro.domain.fauna.model.query.SitePageQuery;
import com.somepro.domain.fauna.repository.MonitorSiteRepository;
import com.somepro.domain.fauna.support.SerialNumbers;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.infrastructure.persistence.fauna.converter.MonitorSitePoConverter;
import com.somepro.infrastructure.persistence.fauna.po.MonitorSitePO;
import com.somepro.infrastructure.persistence.support.AbstractBlockingRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 监测点仓储适配器（基础设施层）。
 */
@Repository
public class MonitorSiteRepositoryImpl extends AbstractBlockingRepository implements MonitorSiteRepository {

    private final MonitorSiteMapper mapper;

    public MonitorSiteRepositoryImpl(MonitorSiteMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Mono<MonitorSite> save(MonitorSite site) {
        return blocking(() -> {
            MonitorSitePO po = MonitorSitePoConverter.toPo(site);
            try {
                if (po.getId() == null) {
                    po.setId(IdUtil.getSnowflakeNextId());
                    mapper.insert(po);
                } else {
                    mapper.updateById(po);
                }
            } catch (DuplicateKeyException e) {
                throw new BizException("监测点编号或登记内容已存在，请勿重复登记");
            }
            return MonitorSitePoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<MonitorSite> findById(Long id) {
        return blocking(() -> {
            MonitorSitePO po = mapper.selectById(id);
            return po == null ? null : MonitorSitePoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<MonitorSite> findByNo(String siteNo) {
        return blocking(() -> {
            MonitorSitePO po = mapper.selectOne(Wrappers.<MonitorSitePO>lambdaQuery()
                    .eq(MonitorSitePO::getSiteNo, siteNo)
                    .last("LIMIT 1"));
            return po == null ? null : MonitorSitePoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<MonitorSite> findDuplicate(Long stationId, MonitorSite.SiteType siteType,
                                           MonitorSite.Habitat habitat, String location) {
        return blocking(() -> {
            // location 可空，用 SQL 的空值口径（<=> 是 NULL 安全等值），保证「空与空」也能判重
            LambdaQueryWrapper<MonitorSitePO> w = Wrappers.<MonitorSitePO>lambdaQuery()
                    .eq(MonitorSitePO::getStationId, stationId)
                    .eq(MonitorSitePO::getSiteType, siteType.name())
                    .eq(MonitorSitePO::getHabitat, habitat.name())
                    .apply("location <=> {0}", location)
                    .last("LIMIT 1");
            MonitorSitePO po = mapper.selectOne(w);
            return po == null ? null : MonitorSitePoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<String> findMaxSiteNo() {
        return blocking(() -> mapper.selectMaxSiteNo(SerialNumbers.sitePrefix() + "%"));
    }

    @Override
    public Mono<Long> countByStationAndStatus(Long stationId, MonitorSite.Status status) {
        return blocking(() -> mapper.selectCount(Wrappers.<MonitorSitePO>lambdaQuery()
                .eq(MonitorSitePO::getStationId, stationId)
                .eq(MonitorSitePO::getStatus, status.name())));
    }

    @Override
    public Mono<PageResult<MonitorSite>> page(int pageNum, int pageSize, SitePageQuery query) {
        return this.<PageResult<MonitorSite>>blocking(() -> {
            try {
                PageHelper.startPage(pageNum, pageSize);
                LambdaQueryWrapper<MonitorSitePO> w = Wrappers.<MonitorSitePO>lambdaQuery()
                        .eq(query.stationId() != null, MonitorSitePO::getStationId, query.stationId())
                        .eq(query.siteNo() != null, MonitorSitePO::getSiteNo, query.siteNo())
                        .eq(query.siteType() != null, MonitorSitePO::getSiteType,
                                query.siteType() == null ? null : query.siteType().name())
                        .eq(query.habitat() != null, MonitorSitePO::getHabitat,
                                query.habitat() == null ? null : query.habitat().name())
                        .eq(query.status() != null, MonitorSitePO::getStatus,
                                query.status() == null ? null : query.status().name())
                        .orderByAsc(MonitorSitePO::getId);
                List<MonitorSitePO> rows = mapper.selectList(w);
                long total = rows instanceof com.github.pagehelper.Page
                        ? ((com.github.pagehelper.Page<?>) rows).getTotal()
                        : rows.size();
                List<MonitorSite> content = rows.stream()
                        .map(MonitorSitePoConverter::toDomain)
                        .collect(Collectors.toList());
                return new PageResult<>(content, total, pageNum, pageSize);
            } finally {
                PageHelper.clearPage();
            }
        });
    }
}

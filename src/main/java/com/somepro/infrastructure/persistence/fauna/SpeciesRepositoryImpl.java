package com.somepro.infrastructure.persistence.fauna;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.somepro.common.exception.BizException;
import com.somepro.domain.fauna.model.Species;
import com.somepro.domain.fauna.model.query.SpeciesPageQuery;
import com.somepro.domain.fauna.repository.SpeciesRepository;
import com.somepro.domain.fauna.support.SerialNumbers;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.infrastructure.persistence.fauna.converter.SpeciesPoConverter;
import com.somepro.infrastructure.persistence.fauna.po.SpeciesPO;
import com.somepro.infrastructure.persistence.support.AbstractBlockingRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 物种名录仓储适配器（基础设施层）。
 */
@Repository
public class SpeciesRepositoryImpl extends AbstractBlockingRepository implements SpeciesRepository {

    private final SpeciesMapper mapper;

    public SpeciesRepositoryImpl(SpeciesMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Mono<Species> save(Species species) {
        return blocking(() -> {
            SpeciesPO po = SpeciesPoConverter.toPo(species);
            try {
                if (po.getId() == null) {
                    po.setId(IdUtil.getSnowflakeNextId());
                    mapper.insert(po);
                } else {
                    mapper.updateById(po);
                }
            } catch (DuplicateKeyException e) {
                throw new BizException("物种编码或名录内容已存在，请勿重复登记");
            }
            return SpeciesPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<Species> findById(Long id) {
        return blocking(() -> {
            SpeciesPO po = mapper.selectById(id);
            return po == null ? null : SpeciesPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<Species> findByCode(String speciesCode) {
        return blocking(() -> {
            SpeciesPO po = mapper.selectOne(Wrappers.<SpeciesPO>lambdaQuery()
                    .eq(SpeciesPO::getSpeciesCode, speciesCode)
                    .last("LIMIT 1"));
            return po == null ? null : SpeciesPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<Species> findDuplicate(String name, String latinName) {
        return blocking(() -> {
            // 学名为可空字段，用 NULL 安全等值（<=>），空与空也算同一物种
            LambdaQueryWrapper<SpeciesPO> w = Wrappers.<SpeciesPO>lambdaQuery()
                    .eq(SpeciesPO::getName, name)
                    .apply("latin_name <=> {0}", latinName)
                    .last("LIMIT 1");
            SpeciesPO po = mapper.selectOne(w);
            return po == null ? null : SpeciesPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<String> findMaxSpeciesCode() {
        return blocking(() -> mapper.selectMaxSpeciesCode(SerialNumbers.speciesPrefix() + "%"));
    }

    @Override
    public Mono<PageResult<Species>> page(int pageNum, int pageSize, SpeciesPageQuery query) {
        return this.<PageResult<Species>>blocking(() -> {
            try {
                PageHelper.startPage(pageNum, pageSize);
                LambdaQueryWrapper<SpeciesPO> w = Wrappers.<SpeciesPO>lambdaQuery()
                        .eq(query.speciesCode() != null, SpeciesPO::getSpeciesCode, query.speciesCode())
                        .like(query.name() != null, SpeciesPO::getName, query.name())
                        .eq(query.protectionLevel() != null, SpeciesPO::getProtectionLevel,
                                query.protectionLevel() == null ? null : query.protectionLevel().name())
                        .eq(query.status() != null, SpeciesPO::getStatus,
                                query.status() == null ? null : query.status().name())
                        .orderByAsc(SpeciesPO::getId);
                List<SpeciesPO> rows = mapper.selectList(w);
                long total = rows instanceof com.github.pagehelper.Page
                        ? ((com.github.pagehelper.Page<?>) rows).getTotal()
                        : rows.size();
                List<Species> content = rows.stream()
                        .map(SpeciesPoConverter::toDomain)
                        .collect(Collectors.toList());
                return new PageResult<>(content, total, pageNum, pageSize);
            } finally {
                PageHelper.clearPage();
            }
        });
    }
}

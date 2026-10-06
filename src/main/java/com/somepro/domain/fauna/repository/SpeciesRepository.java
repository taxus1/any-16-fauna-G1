package com.somepro.domain.fauna.repository;

import com.somepro.domain.fauna.model.Species;
import com.somepro.domain.fauna.model.query.SpeciesPageQuery;
import com.somepro.domain.shared.model.PageResult;
import reactor.core.publisher.Mono;

/**
 * 物种名录仓储端口（领域层定义，基础设施层实现）。
 */
public interface SpeciesRepository {

    Mono<Species> save(Species species);

    Mono<Species> findById(Long id);

    Mono<Species> findByCode(String speciesCode);

    /** 按业务内容查重：同中文名 + 同学名即视为同一物种的重复登记（学名可空，空与空匹配）。 */
    Mono<Species> findDuplicate(String name, String latinName);

    /** 取 SP- 前缀下的最大编码；一条都没有时发空信号。 */
    Mono<String> findMaxSpeciesCode();

    Mono<PageResult<Species>> page(int pageNum, int pageSize, SpeciesPageQuery query);
}

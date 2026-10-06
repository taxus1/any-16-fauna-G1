package com.somepro.application.species;

import com.somepro.common.exception.BizException;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.domain.species.model.Species;
import com.somepro.domain.species.repository.SpeciesRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 物种名录应用层：编排名录用例（录入、改资料、查看、停用、名册分页）。
 *
 * 停用不删除，账还留着；编码全局唯一，撞码由仓储层转业务异常。
 */
@Service
public class SpeciesAppService {

    private final SpeciesRepository speciesRepository;

    public SpeciesAppService(SpeciesRepository speciesRepository) {
        this.speciesRepository = speciesRepository;
    }

    /** 录入物种：保护级别默认一般、状态默认启用；speciesCode 留空时由仓储层生成。 */
    public Mono<Species> createSpecies(String speciesCode, String name, String latinName,
                                       String protectionLevel) {
        Species species = Species.create(name, latinName, protectionLevel);
        species.setSpeciesCode(normalizeCode(speciesCode));
        return speciesRepository.create(species);
    }

    public Mono<Species> updateSpecies(Long id, String name, String latinName, String protectionLevel) {
        return speciesRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("物种不存在")))
                .flatMap(species -> {
                    species.updateProfile(name, latinName, protectionLevel);
                    return speciesRepository.update(species);
                });
    }

    public Mono<Species> detail(Long id) {
        return speciesRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("物种不存在")));
    }

    /** 停用：不删除，名录账还留着。 */
    public Mono<Species> disable(Long id) {
        return speciesRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("物种不存在")))
                .flatMap(species -> {
                    species.disable();
                    return speciesRepository.update(species);
                });
    }

    public Mono<PageResult<Species>> pageSpecies(int pageNum, int pageSize,
                                                 String name, String protectionLevel, String status) {
        return speciesRepository.page(pageNum, pageSize, name, protectionLevel, status);
    }

    private static String normalizeCode(String speciesCode) {
        return (speciesCode == null || speciesCode.isBlank()) ? null : speciesCode.trim();
    }
}

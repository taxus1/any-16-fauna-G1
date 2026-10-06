package com.somepro.application.fauna;

import com.somepro.application.fauna.support.RegisterSerializer;
import com.somepro.common.exception.BizException;
import com.somepro.domain.fauna.model.Species;
import com.somepro.domain.fauna.model.query.SpeciesPageQuery;
import com.somepro.domain.fauna.repository.SpeciesRepository;
import com.somepro.domain.fauna.support.SerialNumbers;
import com.somepro.domain.shared.model.PageResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 物种名录应用服务。
 *
 * 编号规则 SP-0001（无年份段），注册临界区内取最大号 + 1；同内容登记只落一份。
 * 停用只是翻状态、不删账。
 */
@Service
public class SpeciesAppService {

    private static final String REGISTER_LOCK = "fauna:lock:register:species";

    private final SpeciesRepository speciesRepository;
    private final RegisterSerializer registerSerializer;

    public SpeciesAppService(SpeciesRepository speciesRepository, RegisterSerializer registerSerializer) {
        this.speciesRepository = speciesRepository;
        this.registerSerializer = registerSerializer;
    }

    /** 录入物种：保护级别缺省按一般；并发/重复的同一份登记只落一条。 */
    public Mono<Species> enroll(String name, String latinName, String protectionLevel) {
        return registerSerializer.serialize(REGISTER_LOCK, () -> {
            String normalizedName = normalize(name);
            String normalizedLatin = normalize(latinName);
            return speciesRepository.findDuplicate(normalizedName, normalizedLatin)
                    .switchIfEmpty(Mono.defer(() ->
                            nextSpeciesCode()
                                    .map(code -> Species.enroll(
                                            code, normalizedName, normalizedLatin,
                                            protectionLevel == null ? null : protectionLevel.trim()))
                                    .flatMap(speciesRepository::save))
                    .cast(Species.class));
        });
    }

    /** 修改名录信息（中文名 / 学名 / 保护级别）。 */
    public Mono<Species> update(Long id, String name, String latinName, String protectionLevel) {
        return requireSpecies(id).flatMap(species -> {
            species.edit(name, latinName, protectionLevel);
            return speciesRepository.save(species);
        });
    }

    /** 启用 → 停用；账保留。 */
    public Mono<Species> disable(Long id) {
        return changeStatus(id, Species::disable);
    }

    /** 停用 → 启用。 */
    public Mono<Species> enable(Long id) {
        return changeStatus(id, Species::enable);
    }

    public Mono<Species> getById(Long id) {
        return requireSpecies(id);
    }

    public Mono<PageResult<Species>> page(int pageNum, int pageSize, SpeciesPageQuery query) {
        return speciesRepository.page(pageNum, pageSize, query);
    }

    private Mono<Species> changeStatus(Long id, java.util.function.Consumer<Species> transition) {
        return requireSpecies(id).flatMap(species -> {
            transition.accept(species);
            return speciesRepository.save(species);
        });
    }

    private Mono<Species> requireSpecies(Long id) {
        return speciesRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("物种不存在")));
    }

    /** 临界区内取最大号 + 1，生成 SP-0001 编码。 */
    private Mono<String> nextSpeciesCode() {
        return speciesRepository.findMaxSpeciesCode()
                .defaultIfEmpty(SerialNumbers.speciesCode(0))
                .map(max -> {
                    long next = MonitorStationAppService.parseSeq(max, SerialNumbers.speciesPrefix()) + 1;
                    return SerialNumbers.speciesCode(next);
                });
    }

    private static String normalize(String text) {
        return text == null ? null : text.trim().isEmpty() ? null : text.trim();
    }
}

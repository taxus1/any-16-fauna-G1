package com.somepro.interfaces.rest.species;

import com.somepro.application.species.SpeciesAppService;
import com.somepro.common.Result;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.species.converter.SpeciesVoConverter;
import com.somepro.interfaces.rest.species.vo.SpeciesCreateRequest;
import com.somepro.interfaces.rest.species.vo.SpeciesUpdateRequest;
import com.somepro.interfaces.rest.species.vo.SpeciesVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 物种名录接口（用户接口层）：只做协议适配与 VO 转换，业务编排交给应用层。
 */
@RestController
@RequestMapping("/api/species")
public class SpeciesController {

    private final SpeciesAppService speciesAppService;

    public SpeciesController(SpeciesAppService speciesAppService) {
        this.speciesAppService = speciesAppService;
    }

    /** 录入物种：编码可留空由服务端生成，保护级别默认一般，状态默认启用。 */
    @PostMapping
    public Mono<Result<SpeciesVO>> create(@Valid @RequestBody SpeciesCreateRequest req) {
        return speciesAppService.createSpecies(req.speciesCode(), req.name(), req.latinName(),
                        req.protectionLevel())
                .map(SpeciesVoConverter::toVo)
                .map(Result::ok);
    }

    @GetMapping("/{id}")
    public Mono<Result<SpeciesVO>> detail(@PathVariable Long id) {
        return speciesAppService.detail(id)
                .map(SpeciesVoConverter::toVo)
                .map(Result::ok);
    }

    /** 改资料：中文名/学名/保护级别传啥改啥。 */
    @PutMapping("/{id}")
    public Mono<Result<SpeciesVO>> update(@PathVariable Long id,
                                          @RequestBody SpeciesUpdateRequest req) {
        return speciesAppService.updateSpecies(id, req.name(), req.latinName(), req.protectionLevel())
                .map(SpeciesVoConverter::toVo)
                .map(Result::ok);
    }

    /** 停用：不删除，名录账还留着，幂等。 */
    @PostMapping("/{id}/disable")
    public Mono<Result<SpeciesVO>> disable(@PathVariable Long id) {
        return speciesAppService.disable(id)
                .map(SpeciesVoConverter::toVo)
                .map(Result::ok);
    }

    /** 名册分页：name/protectionLevel/status 条件都可空，全空翻整份名册。 */
    @GetMapping({"", "/list"})
    public Mono<Result<PageVO<SpeciesVO>>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "20") int pageSize,
                                                @RequestParam(required = false) String name,
                                                @RequestParam(required = false) String protectionLevel,
                                                @RequestParam(required = false) String status) {
        return speciesAppService.pageSpecies(pageNum, pageSize, name, protectionLevel, status)
                .map(SpeciesVoConverter::toPageVo)
                .map(Result::ok);
    }
}

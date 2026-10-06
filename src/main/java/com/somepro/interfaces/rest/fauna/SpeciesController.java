package com.somepro.interfaces.rest.fauna;

import com.somepro.application.fauna.SpeciesAppService;
import com.somepro.common.Result;
import com.somepro.domain.fauna.model.query.SpeciesPageQuery;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.fauna.command.EnrollSpeciesCommand;
import com.somepro.interfaces.rest.fauna.command.UpdateSpeciesCommand;
import com.somepro.interfaces.rest.fauna.converter.SpeciesVoConverter;
import com.somepro.interfaces.rest.fauna.vo.SpeciesVO;
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
 * 物种名录接口（用户接口层）。
 *
 *  - POST /api/fauna/species             录入（编码 SP-0001 自动生成，保护级别缺省一般，默认启用）
 *  - PUT  /api/fauna/species/{id}        改档
 *  - GET  /api/fauna/species             分页名录（可按编码、中文名、级别、状态过滤）
 *  - GET  /api/fauna/species/{id}        详情
 *  - POST /api/fauna/species/{id}/disable | /enable 停用（不删账）/ 启用
 */
@RestController
@RequestMapping("/api/fauna/species")
public class SpeciesController {

    private final SpeciesAppService appService;

    public SpeciesController(SpeciesAppService appService) {
        this.appService = appService;
    }

    @PostMapping
    public Mono<Result<SpeciesVO>> enroll(@Valid @RequestBody EnrollSpeciesCommand cmd) {
        return appService.enroll(cmd.name(), cmd.latinName(), cmd.protectionLevel())
                .map(SpeciesVoConverter::toVo)
                .map(Result::ok);
    }

    @PutMapping("/{id}")
    public Mono<Result<SpeciesVO>> update(@PathVariable Long id,
                                          @Valid @RequestBody UpdateSpeciesCommand cmd) {
        return appService.update(id, cmd.name(), cmd.latinName(), cmd.protectionLevel())
                .map(SpeciesVoConverter::toVo)
                .map(Result::ok);
    }

    @GetMapping
    public Mono<Result<PageVO<SpeciesVO>>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String speciesCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String protectionLevel,
            @RequestParam(required = false) String status) {
        SpeciesPageQuery query = SpeciesPageQuery.of(speciesCode, name, protectionLevel, status);
        return appService.page(sanitizePageNum(pageNum), sanitizePageSize(pageSize), query)
                .map(SpeciesVoConverter::toPageVo)
                .map(Result::ok);
    }

    @GetMapping("/{id}")
    public Mono<Result<SpeciesVO>> get(@PathVariable Long id) {
        return appService.getById(id)
                .map(SpeciesVoConverter::toVo)
                .map(Result::ok);
    }

    @PostMapping("/{id}/disable")
    public Mono<Result<SpeciesVO>> disable(@PathVariable Long id) {
        return appService.disable(id).map(SpeciesVoConverter::toVo).map(Result::ok);
    }

    @PostMapping("/{id}/enable")
    public Mono<Result<SpeciesVO>> enable(@PathVariable Long id) {
        return appService.enable(id).map(SpeciesVoConverter::toVo).map(Result::ok);
    }

    private static int sanitizePageNum(int pageNum) {
        return pageNum < 1 ? 1 : pageNum;
    }

    private static int sanitizePageSize(int pageSize) {
        if (pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 500);
    }
}

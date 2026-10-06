package com.somepro.interfaces.rest.fauna;

import com.somepro.application.fauna.MonitorSiteAppService;
import com.somepro.common.Result;
import com.somepro.domain.fauna.model.query.SitePageQuery;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.fauna.command.RegisterSiteCommand;
import com.somepro.interfaces.rest.fauna.command.UpdateSiteCommand;
import com.somepro.interfaces.rest.fauna.converter.SiteVoConverter;
import com.somepro.interfaces.rest.fauna.vo.SiteVO;
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
 * 监测点接口（用户接口层）。
 *
 *  - POST /api/fauna/sites             登记（编号 MP-2026-0001 自动生成，默认在册；
 *                                       只能挂到运行中的站，停用/关闭/不存在的站一律拒绝）
 *  - PUT  /api/fauna/sites/{id}        改档（类型/生境/位置，不改归属）
 *  - GET  /api/fauna/sites             分页名单（可按站、类型、生境、状态过滤）
 *  - GET  /api/fauna/sites/{id}        详情
 *  - POST /api/fauna/sites/{id}/deactivate | /activate 停测 / 恢复在册
 */
@RestController
@RequestMapping("/api/fauna/sites")
public class MonitorSiteController {

    private final MonitorSiteAppService appService;

    public MonitorSiteController(MonitorSiteAppService appService) {
        this.appService = appService;
    }

    @PostMapping
    public Mono<Result<SiteVO>> register(@Valid @RequestBody RegisterSiteCommand cmd) {
        return appService.register(cmd.stationId(), cmd.siteType(), cmd.habitat(), cmd.location())
                .map(SiteVoConverter::toVo)
                .map(Result::ok);
    }

    @PutMapping("/{id}")
    public Mono<Result<SiteVO>> update(@PathVariable Long id,
                                       @Valid @RequestBody UpdateSiteCommand cmd) {
        return appService.update(id, cmd.siteType(), cmd.habitat(), cmd.location())
                .map(SiteVoConverter::toVo)
                .map(Result::ok);
    }

    @GetMapping
    public Mono<Result<PageVO<SiteVO>>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) String siteNo,
            @RequestParam(required = false) String siteType,
            @RequestParam(required = false) String habitat,
            @RequestParam(required = false) String status) {
        SitePageQuery query = SitePageQuery.of(stationId, siteNo, siteType, habitat, status);
        return appService.page(sanitizePageNum(pageNum), sanitizePageSize(pageSize), query)
                .map(SiteVoConverter::toPageVo)
                .map(Result::ok);
    }

    @GetMapping("/{id}")
    public Mono<Result<SiteVO>> get(@PathVariable Long id) {
        return appService.getById(id)
                .map(SiteVoConverter::toVo)
                .map(Result::ok);
    }

    @PostMapping("/{id}/deactivate")
    public Mono<Result<SiteVO>> deactivate(@PathVariable Long id) {
        return appService.deactivate(id).map(SiteVoConverter::toVo).map(Result::ok);
    }

    @PostMapping("/{id}/activate")
    public Mono<Result<SiteVO>> activate(@PathVariable Long id) {
        return appService.activate(id).map(SiteVoConverter::toVo).map(Result::ok);
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

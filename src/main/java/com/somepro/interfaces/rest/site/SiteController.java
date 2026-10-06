package com.somepro.interfaces.rest.site;

import com.somepro.application.site.MonitorSiteAppService;
import com.somepro.common.Result;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.site.converter.SiteVoConverter;
import com.somepro.interfaces.rest.site.vo.SiteCreateRequest;
import com.somepro.interfaces.rest.site.vo.SiteUpdateRequest;
import com.somepro.interfaces.rest.site.vo.SiteVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * 监测点接口（用户接口层）：只做协议适配与 VO 转换，业务编排交给应用层。
 */
@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final MonitorSiteAppService siteAppService;

    public SiteController(MonitorSiteAppService siteAppService) {
        this.siteAppService = siteAppService;
    }

    /** 登记监测点：编号可留空由服务端生成，默认在册。 */
    @PostMapping
    public Mono<Result<SiteVO>> create(@Valid @RequestBody SiteCreateRequest req) {
        return siteAppService.createSite(req.siteNo(), req.stationId(), req.siteType(),
                        req.habitat(), req.location())
                .map(SiteVoConverter::toVo)
                .map(Result::ok);
    }

    @GetMapping("/{id}")
    public Mono<Result<SiteVO>> detail(@PathVariable Long id) {
        return siteAppService.detail(id)
                .map(SiteVoConverter::toVo)
                .map(Result::ok);
    }

    /** 改资料：类型/生境/位置传啥改啥；改挂目标站要过挂载校验。 */
    @PutMapping("/{id}")
    public Mono<Result<SiteVO>> update(@PathVariable Long id,
                                       @RequestBody SiteUpdateRequest req) {
        return siteAppService.updateSite(id, req.stationId(), req.siteType(),
                        req.habitat(), req.location())
                .map(SiteVoConverter::toVo)
                .map(Result::ok);
    }

    /** 停测：在册 -> 停测，幂等。 */
    @PostMapping("/{id}/deactivate")
    public Mono<Result<SiteVO>> deactivate(@PathVariable Long id) {
        return siteAppService.deactivate(id)
                .map(SiteVoConverter::toVo)
                .map(Result::ok);
    }

    /** 恢复在册：停测 -> 在册，幂等。 */
    @PostMapping("/{id}/activate")
    public Mono<Result<SiteVO>> activate(@PathVariable Long id) {
        return siteAppService.activate(id)
                .map(SiteVoConverter::toVo)
                .map(Result::ok);
    }

    /** 撤点：逻辑删除，账留在表里。 */
    @DeleteMapping("/{id}")
    public Mono<Result<Void>> remove(@PathVariable Long id) {
        return siteAppService.remove(id)
                .then(Mono.just(Result.ok()));
    }

    /** 名册分页：stationId/siteType/habitat/status 条件都可空，全空翻整份名册。 */
    @GetMapping({"", "/list"})
    public Mono<Result<PageVO<SiteVO>>> page(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "20") int pageSize,
                                             @RequestParam(required = false) Long stationId,
                                             @RequestParam(required = false) String siteType,
                                             @RequestParam(required = false) String habitat,
                                             @RequestParam(required = false) String status) {
        return siteAppService.pageSites(pageNum, pageSize, stationId, siteType, habitat, status)
                .map(SiteVoConverter::toPageVo)
                .map(Result::ok);
    }
}

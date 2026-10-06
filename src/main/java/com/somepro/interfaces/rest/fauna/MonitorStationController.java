package com.somepro.interfaces.rest.fauna;

import com.somepro.application.fauna.MonitorStationAppService;
import com.somepro.common.Result;
import com.somepro.domain.fauna.model.query.StationPageQuery;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.fauna.command.RegisterStationCommand;
import com.somepro.interfaces.rest.fauna.command.UpdateStationCommand;
import com.somepro.interfaces.rest.fauna.converter.StationVoConverter;
import com.somepro.interfaces.rest.fauna.vo.StationDetailVO;
import com.somepro.interfaces.rest.fauna.vo.StationVO;
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
 * 监测站接口（用户接口层）：只做协议适配与 VO 转换，业务编排交给应用层。
 *
 *  - POST   /api/fauna/stations             登记（编号 ST-2026-0001 自动生成，默认运行）
 *  - PUT    /api/fauna/stations/{id}        改档
 *  - GET    /api/fauna/stations             分页名单（条件全可选，不填即全量按页翻）
 *  - GET    /api/fauna/stations/{id}        档案
 *  - GET    /api/fauna/stations/{id}/detail 档案 + 名下在册/停测点位数
 *  - POST   /api/fauna/stations/{id}/suspend | /resume | /close 状态流转
 */
@RestController
@RequestMapping("/api/fauna/stations")
public class MonitorStationController {

    private final MonitorStationAppService appService;

    public MonitorStationController(MonitorStationAppService appService) {
        this.appService = appService;
    }

    @PostMapping
    public Mono<Result<StationVO>> register(@Valid @RequestBody RegisterStationCommand cmd) {
        return appService.register(cmd.name(), cmd.level(), cmd.region(), cmd.leader(), cmd.phone())
                .map(StationVoConverter::toVo)
                .map(Result::ok);
    }

    @PutMapping("/{id}")
    public Mono<Result<StationVO>> update(@PathVariable Long id,
                                          @Valid @RequestBody UpdateStationCommand cmd) {
        return appService.update(id, cmd.name(), cmd.level(), cmd.region(), cmd.leader(), cmd.phone())
                .map(StationVoConverter::toVo)
                .map(Result::ok);
    }

    @GetMapping
    public Mono<Result<PageVO<StationVO>>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String stationNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status) {
        StationPageQuery query = StationPageQuery.of(stationNo, name, region, level, status);
        return appService.page(sanitizePageNum(pageNum), sanitizePageSize(pageSize), query)
                .map(StationVoConverter::toPageVo)
                .map(Result::ok);
    }

    @GetMapping("/{id}")
    public Mono<Result<StationVO>> get(@PathVariable Long id) {
        return appService.getById(id)
                .map(StationVoConverter::toVo)
                .map(Result::ok);
    }

    @GetMapping("/{id}/detail")
    public Mono<Result<StationDetailVO>> detail(@PathVariable Long id) {
        return appService.getDetail(id)
                .map(StationVoConverter::toDetailVo)
                .map(Result::ok);
    }

    @PostMapping("/{id}/suspend")
    public Mono<Result<StationVO>> suspend(@PathVariable Long id) {
        return appService.suspend(id).map(StationVoConverter::toVo).map(Result::ok);
    }

    @PostMapping("/{id}/resume")
    public Mono<Result<StationVO>> resume(@PathVariable Long id) {
        return appService.resume(id).map(StationVoConverter::toVo).map(Result::ok);
    }

    @PostMapping("/{id}/close")
    public Mono<Result<StationVO>> close(@PathVariable Long id) {
        return appService.close(id).map(StationVoConverter::toVo).map(Result::ok);
    }

    private static int sanitizePageNum(int pageNum) {
        return pageNum < 1 ? 1 : pageNum;
    }

    private static int sanitizePageSize(int pageSize) {
        if (pageSize < 1) {
            return 20;
        }
        // 每页几条由请求说了算，但兜一个上限，防止一次把整库拖走
        return Math.min(pageSize, 500);
    }
}

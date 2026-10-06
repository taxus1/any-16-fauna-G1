package com.somepro.interfaces.rest.station;

import com.somepro.application.station.MonitorStationAppService;
import com.somepro.common.Result;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.station.converter.StationVoConverter;
import com.somepro.interfaces.rest.station.vo.StationCreateRequest;
import com.somepro.interfaces.rest.station.vo.StationDetailVO;
import com.somepro.interfaces.rest.station.vo.StationUpdateRequest;
import com.somepro.interfaces.rest.station.vo.StationVO;
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
 * 名册分页：条件都可空，全空时返回整份名册；pageNum/pageSize 由请求方说了算。
 */
@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final MonitorStationAppService stationAppService;

    public StationController(MonitorStationAppService stationAppService) {
        this.stationAppService = stationAppService;
    }

    /** 新立监测站：编号可留空由服务端生成，默认运行状态。 */
    @PostMapping
    public Mono<Result<StationVO>> create(@Valid @RequestBody StationCreateRequest req) {
        return stationAppService.createStation(req.stationNo(), req.name(), req.level(),
                        req.region(), req.leader(), req.phone())
                .map(StationVoConverter::toVo)
                .map(Result::ok);
    }

    /** 站详情：连同名下在册/停测点位数一起带出。 */
    @GetMapping("/{id}")
    public Mono<Result<StationDetailVO>> detail(@PathVariable Long id) {
        return stationAppService.detail(id)
                .map(StationVoConverter::toDetailVo)
                .map(Result::ok);
    }

    /** 改资料：传啥改啥；已关闭的站不能再改。 */
    @PutMapping("/{id}")
    public Mono<Result<StationVO>> update(@PathVariable Long id,
                                          @RequestBody StationUpdateRequest req) {
        return stationAppService.updateStation(id, req.name(), req.level(),
                        req.region(), req.leader(), req.phone())
                .map(StationVoConverter::toVo)
                .map(Result::ok);
    }

    /** 停用：运行 -> 停用，幂等。 */
    @PostMapping("/{id}/suspend")
    public Mono<Result<StationVO>> suspend(@PathVariable Long id) {
        return stationAppService.suspend(id)
                .map(StationVoConverter::toVo)
                .map(Result::ok);
    }

    /** 关闭：名下还有在册点位时不允许关；重复关闭幂等。 */
    @PostMapping("/{id}/close")
    public Mono<Result<StationVO>> close(@PathVariable Long id) {
        return stationAppService.close(id)
                .map(StationVoConverter::toVo)
                .map(Result::ok);
    }

    /** 名册分页：name/level/region/status 条件都可空，全空翻整份名册。 */
    @GetMapping({"", "/list"})
    public Mono<Result<PageVO<StationVO>>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "20") int pageSize,
                                                @RequestParam(required = false) String name,
                                                @RequestParam(required = false) String level,
                                                @RequestParam(required = false) String region,
                                                @RequestParam(required = false) String status) {
        return stationAppService.pageStations(pageNum, pageSize, name, level, region, status)
                .map(StationVoConverter::toPageVo)
                .map(Result::ok);
    }
}

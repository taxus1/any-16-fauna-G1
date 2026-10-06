package com.somepro.interfaces.rest.station.vo;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 新立监测站请求（VO，用户接口层）。
 *
 * stationNo 可空：空则由服务端按 ST-YYYY-NNNN 生成；显式指定时撞号返回业务失败，不甩底层错。
 * status 不接受入参 —— 新立的站一律默认运行（ACTIVE）。
 */
public record StationCreateRequest(
        String stationNo,
        @NotBlank(message = "监测站名称不能为空") String name,
        @NotBlank(message = "层级不能为空") String level,
        @NotBlank(message = "所在辖区不能为空") String region,
        String leader,
        String phone) implements Serializable {
}

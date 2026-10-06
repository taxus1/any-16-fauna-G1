package com.somepro.interfaces.rest.site.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 登记监测点请求（VO，用户接口层）。
 *
 * siteNo 可空：空则由服务端按 MP-YYYY-NNNN 生成。
 * 目标站必须存在且未停用未关闭（应用层校验）。
 */
public record SiteCreateRequest(
        String siteNo,
        @NotNull(message = "所属监测站不能为空") Long stationId,
        @NotBlank(message = "监测点类型不能为空") String siteType,
        @NotBlank(message = "生境不能为空") String habitat,
        String location) implements Serializable {
}

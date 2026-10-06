package com.somepro.interfaces.rest.site.vo;

import java.io.Serializable;

/**
 * 修改监测点请求（VO，用户接口层）：字段都可空，传啥改啥。
 * stationId 与当前不同视为改挂，目标站同样要过挂载校验。
 */
public record SiteUpdateRequest(
        Long stationId,
        String siteType,
        String habitat,
        String location) implements Serializable {
}

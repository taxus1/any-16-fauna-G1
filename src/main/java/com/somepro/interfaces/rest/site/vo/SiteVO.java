package com.somepro.interfaces.rest.site.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 监测点对外返回对象（VO，用户接口层）—— 不可变 record。编号 siteNo 必带。
 */
public record SiteVO(Long id, String siteNo, Long stationId, String siteType, String habitat,
                     String location, String status,
                     LocalDateTime createTime) implements Serializable {
}

package com.somepro.interfaces.rest.station.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 监测站对外返回对象（VO，用户接口层）—— 不可变 record。
 *
 * 只暴露对外字段：编号 stationNo 必带（名册每行都要编号）；
 * delFlag / createBy / updateBy / updateTime 等内部字段不进 API 契约。
 */
public record StationVO(Long id, String stationNo, String name, String level, String region,
                        String leader, String phone, String status,
                        LocalDateTime createTime) implements Serializable {
}

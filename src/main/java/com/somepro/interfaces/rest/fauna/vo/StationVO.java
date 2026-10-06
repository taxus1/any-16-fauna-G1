package com.somepro.interfaces.rest.fauna.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 监测站对外 VO（不可变 record）。每行带编号 stationNo；不暴露 delFlag 等内部字段。
 */
public record StationVO(Long id, String stationNo, String name, String level, String region,
                        String leader, String phone, String status, LocalDateTime createTime)
        implements Serializable {
}

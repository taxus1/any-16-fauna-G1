package com.somepro.interfaces.rest.station.vo;

import java.io.Serializable;

/**
 * 修改监测站请求（VO，用户接口层）：字段都可空，传啥改啥（null 表示不动）。
 * 编号与状态不走这里改 —— 编号不可变，状态走 suspend/close 专用动作。
 */
public record StationUpdateRequest(
        String name,
        String level,
        String region,
        String leader,
        String phone) implements Serializable {
}

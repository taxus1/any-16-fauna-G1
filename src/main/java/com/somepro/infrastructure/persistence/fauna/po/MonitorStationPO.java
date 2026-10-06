package com.somepro.infrastructure.persistence.fauna.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.somepro.infrastructure.persistence.base.BasePO;
import lombok.Getter;
import lombok.Setter;

/**
 * t_monitor_station 表的持久化对象（PO，基础设施层）。
 * 只描述表形状，业务规则在领域对象 MonitorStation。
 */
@Getter
@Setter
@TableName("t_monitor_station")
public class MonitorStationPO extends BasePO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("station_no")
    private String stationNo;

    @TableField("name")
    private String name;

    @TableField("level")
    private String level;

    @TableField("region")
    private String region;

    @TableField("leader")
    private String leader;

    @TableField("phone")
    private String phone;

    @TableField("status")
    private String status;
}

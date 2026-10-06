package com.somepro.infrastructure.persistence.site.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.somepro.infrastructure.persistence.base.BasePO;
import lombok.Getter;
import lombok.Setter;

/**
 * t_monitor_site 表的持久化对象（PO，基础设施层）。
 *
 * 只描述「表长什么样」，业务规则在领域对象 MonitorSite。
 */
@Getter
@Setter
@TableName("t_monitor_site")
public class MonitorSitePO extends BasePO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("site_no")
    private String siteNo;

    @TableField("station_id")
    private Long stationId;

    @TableField("site_type")
    private String siteType;

    @TableField("habitat")
    private String habitat;

    @TableField("location")
    private String location;

    @TableField("status")
    private String status;
}

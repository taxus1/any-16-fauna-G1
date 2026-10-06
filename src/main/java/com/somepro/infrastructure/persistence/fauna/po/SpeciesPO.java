package com.somepro.infrastructure.persistence.fauna.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.somepro.infrastructure.persistence.base.BasePO;
import lombok.Getter;
import lombok.Setter;

/**
 * t_species 表的持久化对象（PO，基础设施层）。
 * 只描述表形状，业务规则在领域对象 Species。
 */
@Getter
@Setter
@TableName("t_species")
public class SpeciesPO extends BasePO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("species_code")
    private String speciesCode;

    @TableField("name")
    private String name;

    @TableField("latin_name")
    private String latinName;

    @TableField("protection_level")
    private String protectionLevel;

    @TableField("status")
    private String status;
}

package com.somepro.interfaces.rest.fauna.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 监测点改档命令：只改类型 / 生境 / 位置说明，归属站不能通过编辑改挂。
 */
public record UpdateSiteCommand(
        @NotNull(message = "点位类型不能为空")
        @Pattern(regexp = "TRANSECT|SAMPLE_POINT|CAMERA", message = "点位类型取值非法")
        String siteType,

        @NotNull(message = "生境不能为空")
        @Pattern(regexp = "FOREST|WETLAND|GRASSLAND|FARMLAND|DESERT", message = "生境取值非法")
        String habitat,

        @Size(max = 255, message = "位置说明最长 255 字")
        String location) {
}

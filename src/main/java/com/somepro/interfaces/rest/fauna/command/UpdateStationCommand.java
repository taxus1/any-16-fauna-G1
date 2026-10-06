package com.somepro.interfaces.rest.fauna.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 监测站改档命令：可改名 / 层级 / 辖区 / 负责人 / 电话，状态走专用接口。
 */
public record UpdateStationCommand(
        @NotBlank(message = "监测站名称不能为空")
        @Size(max = 128, message = "监测站名称最长 128 字")
        String name,

        @NotBlank(message = "层级不能为空")
        @Pattern(regexp = "PROVINCIAL|MUNICIPAL|COUNTY", message = "层级取值非法")
        String level,

        @NotBlank(message = "所在辖区不能为空")
        @Size(max = 128, message = "所在辖区最长 128 字")
        String region,

        @Size(max = 64, message = "负责人最长 64 字")
        String leader,

        @Size(max = 20, message = "联系电话最长 20 字")
        String phone) {
}

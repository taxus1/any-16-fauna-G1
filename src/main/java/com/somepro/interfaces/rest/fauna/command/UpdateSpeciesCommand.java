package com.somepro.interfaces.rest.fauna.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 物种改档命令：中文名 / 学名 / 保护级别；保护级别缺省回落到一般。
 */
public record UpdateSpeciesCommand(
        @NotBlank(message = "中文名不能为空")
        @Size(max = 64, message = "中文名最长 64 字")
        String name,

        @Size(max = 128, message = "学名最长 128 字")
        String latinName,

        @Pattern(regexp = "NATIONAL_ONE|NATIONAL_TWO|PROVINCIAL|COMMON",
                message = "保护级别取值非法")
        String protectionLevel) {
}

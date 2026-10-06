package com.somepro.interfaces.rest.species.vo;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 录入物种请求（VO，用户接口层）。
 *
 * speciesCode 可空：空则由服务端按 SP-NNNN 生成；protectionLevel 可空，默认一般（COMMON）。
 */
public record SpeciesCreateRequest(
        String speciesCode,
        @NotBlank(message = "物种中文名不能为空") String name,
        String latinName,
        String protectionLevel) implements Serializable {
}

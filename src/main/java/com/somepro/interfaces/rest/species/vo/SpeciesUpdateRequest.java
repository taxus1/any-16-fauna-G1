package com.somepro.interfaces.rest.species.vo;

import java.io.Serializable;

/**
 * 修改物种请求（VO，用户接口层）：字段都可空，传啥改啥。编码不可变，状态走 disable 专用动作。
 */
public record SpeciesUpdateRequest(
        String name,
        String latinName,
        String protectionLevel) implements Serializable {
}

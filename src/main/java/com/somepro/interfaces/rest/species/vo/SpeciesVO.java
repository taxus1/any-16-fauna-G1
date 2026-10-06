package com.somepro.interfaces.rest.species.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物种对外返回对象（VO，用户接口层）—— 不可变 record。编码 speciesCode 必带。
 */
public record SpeciesVO(Long id, String speciesCode, String name, String latinName,
                        String protectionLevel, String status,
                        LocalDateTime createTime) implements Serializable {
}

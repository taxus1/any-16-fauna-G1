package com.somepro.interfaces.rest.fauna.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物种名录对外 VO（不可变 record）。每行带编码 speciesCode；停用不删，状态照常返回。
 */
public record SpeciesVO(Long id, String speciesCode, String name, String latinName,
                        String protectionLevel, String status, LocalDateTime createTime)
        implements Serializable {
}

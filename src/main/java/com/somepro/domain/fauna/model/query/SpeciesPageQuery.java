package com.somepro.domain.fauna.model.query;

import com.somepro.domain.fauna.model.Species;
import com.somepro.domain.shared.support.DomainChecks;

/**
 * 物种名录分页查询条件（领域值对象）。
 *
 * 可按编码、中文名、保护级别、状态过滤；全部缺省即翻整份名录。
 */
public record SpeciesPageQuery(String speciesCode, String name,
                               Species.ProtectionLevel protectionLevel, Species.Status status) {

    public static SpeciesPageQuery of(String speciesCode, String name,
                                      String protectionLevel, String status) {
        return new SpeciesPageQuery(
                speciesCode == null || speciesCode.isBlank() ? null : speciesCode.trim(),
                name == null || name.isBlank() ? null : name.trim(),
                protectionLevel == null || protectionLevel.isBlank()
                        ? null : DomainChecks.parseEnum(Species.ProtectionLevel.class, protectionLevel, "保护级别"),
                status == null || status.isBlank()
                        ? null : DomainChecks.parseEnum(Species.Status.class, status, "状态"));
    }
}

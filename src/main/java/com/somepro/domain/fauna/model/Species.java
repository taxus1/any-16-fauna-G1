package com.somepro.domain.fauna.model;

import com.somepro.common.exception.BizException;
import com.somepro.domain.shared.model.BaseEntity;
import com.somepro.domain.shared.support.DomainChecks;
import lombok.Getter;
import lombok.Setter;

/**
 * 物种名录聚合根（fauna 限界上下文）：观测要挂的物种底册。
 *
 * 纯领域对象，不带任何持久化注解。
 *
 * 不变量：
 * - 编码（speciesCode）全局唯一，按 SP-0001 规则由应用侧生成，一个编码只归一个物种；
 * - 中文名必填、学名可空；
 * - 保护级别四档，见 {@link ProtectionLevel}，新录默认 COMMON；
 * - 状态两档 ENABLED / DISABLED；停用不删账，只是不能再被新观测引用（由后续观测模块把关）。
 */
@Getter
@Setter
public class Species extends BaseEntity {

    /** 保护级别：国家一级 / 国家二级 / 省级 / 一般。 */
    public enum ProtectionLevel {
        NATIONAL_ONE,
        NATIONAL_TWO,
        PROVINCIAL,
        COMMON
    }

    /** 状态：启用 / 停用。 */
    public enum Status {
        ENABLED,
        DISABLED
    }

    private Long id;

    /** 物种编码，全局唯一（如 SP-0001），录入时由应用侧分配。 */
    private String speciesCode;

    /** 中文名。 */
    private String name;

    /** 学名。 */
    private String latinName;

    private ProtectionLevel protectionLevel;

    private Status status;

    /** 工厂方法：新录物种，保护级别默认一般、状态默认启用。 */
    public static Species enroll(String speciesCode, String name, String latinName, String protectionLevel) {
        Species species = new Species();
        species.speciesCode = DomainChecks.requireText(speciesCode, "物种编码");
        species.edit(name, latinName, protectionLevel);
        species.status = Status.ENABLED;
        return species;
    }

    /** 修改名录信息（中文名 / 学名 / 保护级别），不碰状态。级别缺省回落到一般。 */
    public void edit(String name, String latinName, String protectionLevel) {
        this.name = DomainChecks.requireText(name, "中文名");
        this.latinName = DomainChecks.optionalText(latinName);
        this.protectionLevel = (protectionLevel == null || protectionLevel.isBlank())
                ? ProtectionLevel.COMMON
                : DomainChecks.parseEnum(ProtectionLevel.class, protectionLevel, "保护级别");
    }

    /** 启用 → 停用。账还留着，不做物理删除。 */
    public void disable() {
        if (status == Status.DISABLED) {
            throw new BizException("物种已停用，无需重复停用");
        }
        this.status = Status.DISABLED;
    }

    /** 停用 → 启用。 */
    public void enable() {
        if (status == Status.ENABLED) {
            throw new BizException("物种已启用，无需重复启用");
        }
        this.status = Status.ENABLED;
    }
}

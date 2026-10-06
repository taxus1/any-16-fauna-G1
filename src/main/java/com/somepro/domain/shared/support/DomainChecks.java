package com.somepro.domain.shared.support;

import com.somepro.common.exception.BizException;

/**
 * 领域层共用的入参校验小工具（纯领域，不依赖任何框架）。
 *
 * 集中在这里而不是各聚合里各写一份：枚举解析、文本必填、空白归一化的规则全项目统一，
 * 非法输入一律抛 {@link BizException}（由全局异常处理成统一 Result，不甩底层异常给调用方）。
 */
public final class DomainChecks {

    private DomainChecks() {
    }

    /** 必填文本：去空白后不能为空，返回归一化后的值。 */
    public static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new BizException(label + "不能为空");
        }
        return value.trim();
    }

    /** 可空文本：null 保持 null，非 null 去空白，空白串归一化为 null。 */
    public static String optionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 按枚举常量名解析编码；非法/缺失编码给明确的业务报错，
     * 而不是让 {@link Enum#valueOf} 的 IllegalArgumentException 漏到接口上。
     */
    public static <E extends Enum<E>> E parseEnum(Class<E> type, String code, String label) {
        if (code == null || code.isBlank()) {
            throw new BizException(label + "不能为空");
        }
        try {
            return Enum.valueOf(type, code.trim());
        } catch (IllegalArgumentException e) {
            throw new BizException(label + "取值非法：" + code.trim());
        }
    }
}

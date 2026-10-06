package com.somepro.domain.fauna.support;

/**
 * 三块底账的业务编号规则（纯领域）：
 * - 监测站：ST-2026-0001（4 位流水）
 * - 监测点：MP-2026-0001（4 位流水）
 * - 物种：SP-0001（4 位流水，无年份段）
 *
 * 流水号取库里已有最大编号 + 1，定宽 4 位、左侧补零。
 * 字符串上直接取最大值即可（前缀 + 定宽流水保证字典序与数值序一致），
 * 与仓储里「按前缀 LIKE 取 MAX」配套；并发生成由应用侧分布式锁串住。
 */
public final class SerialNumbers {

    /** 当前年份段：题目编号口径为 2026。 */
    public static final String YEAR = "2026";

    /** 流水号宽度。 */
    private static final int SEQ_WIDTH = 4;

    private SerialNumbers() {
    }

    public static String stationNo(long seq) {
        return "ST-" + YEAR + "-" + pad(seq);
    }

    public static String siteNo(long seq) {
        return "MP-" + YEAR + "-" + pad(seq);
    }

    public static String speciesCode(long seq) {
        return "SP-" + pad(seq);
    }

    /** 监测站编号前缀，仓储按它 LIKE 取当前年份段的最大编号。 */
    public static String stationPrefix() {
        return "ST-" + YEAR + "-";
    }

    public static String sitePrefix() {
        return "MP-" + YEAR + "-";
    }

    public static String speciesPrefix() {
        return "SP-";
    }

    /** 流水宽度超出 4 位（超过 9999 条）时不再截断，避免编号撞车。 */
    private static String pad(long seq) {
        String digits = Long.toString(seq);
        if (digits.length() >= SEQ_WIDTH) {
            return digits;
        }
        return "0".repeat(SEQ_WIDTH - digits.length()) + digits;
    }
}

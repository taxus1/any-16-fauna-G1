package com.somepro.domain.fauna.model;

import com.somepro.common.exception.BizException;
import com.somepro.domain.shared.model.BaseEntity;
import com.somepro.domain.shared.support.DomainChecks;
import lombok.Getter;
import lombok.Setter;

/**
 * 监测站聚合根（fauna 限界上下文）。
 *
 * 纯领域对象，不带任何持久化注解；落库由基础设施层 MonitorStationPO 承载。
 *
 * 不变量：
 * - 编号（stationNo）全局唯一，编号由应用侧按 ST-2026-0001 规则生成，聚合不负责生成；
 * - 层级三档、状态三档，取值见 {@link Level} / {@link Status}；
 * - 新立的站默认 ACTIVE；
 * - 状态流转必须走显式领域行为，不允许直接 setStatus 翻来覆去：
 *   ACTIVE→SUSPENDED / ACTIVE→CLOSED / SUSPENDED→ACTIVE / SUSPENDED→CLOSED，
 *   CLOSED 为终态；对当前状态重复发起同样的动作直接报错（状态要稳）。
 */
@Getter
@Setter
public class MonitorStation extends BaseEntity {

    /** 层级：省级 / 市级 / 县级。 */
    public enum Level {
        PROVINCIAL,
        MUNICIPAL,
        COUNTY
    }

    /** 状态：运行 / 停用 / 关闭。 */
    public enum Status {
        ACTIVE,
        SUSPENDED,
        CLOSED
    }

    private Long id;

    /** 监测站编号，全局唯一（如 ST-2026-0001），注册时由应用侧分配。 */
    private String stationNo;

    private String name;

    private Level level;

    /** 所在辖区。 */
    private String region;

    private String leader;

    private String phone;

    private Status status;

    /** 工厂方法：新立监测站，默认运行。 */
    public static MonitorStation register(String stationNo, String name, String level,
                                          String region, String leader, String phone) {
        MonitorStation station = new MonitorStation();
        station.stationNo = DomainChecks.requireText(stationNo, "监测站编号");
        station.edit(name, level, region, leader, phone);
        station.status = Status.ACTIVE;
        return station;
    }

    /** 修改档案信息（站名 / 层级 / 辖区 / 负责人 / 联系电话），不碰状态。 */
    public void edit(String name, String level, String region, String leader, String phone) {
        this.name = DomainChecks.requireText(name, "监测站名称");
        this.level = DomainChecks.parseEnum(Level.class, level, "层级");
        this.region = DomainChecks.requireText(region, "所在辖区");
        this.leader = DomainChecks.optionalText(leader);
        this.phone = DomainChecks.optionalText(phone);
    }

    /** 运行 → 停用。已经停用/关闭的站再点停用直接拒绝，避免状态被随手翻。 */
    public void suspend() {
        if (status == Status.SUSPENDED) {
            throw new BizException("监测站已是停用状态，无需重复停用");
        }
        if (status == Status.CLOSED) {
            throw new BizException("监测站已关闭，不能停用");
        }
        this.status = Status.SUSPENDED;
    }

    /** 停用 → 恢复运行。 */
    public void resume() {
        if (status == Status.ACTIVE) {
            throw new BizException("监测站正在运行，无需恢复");
        }
        if (status == Status.CLOSED) {
            throw new BizException("监测站已关闭，不能恢复运行");
        }
        this.status = Status.ACTIVE;
    }

    /**
     * 关闭。CLOSED 为终态，关之前应用层还要核对名下没有在册点位，
     * 点位都停测或撤了才允许关。
     */
    public void close() {
        if (status == Status.CLOSED) {
            throw new BizException("监测站已关闭，无需重复关闭");
        }
        this.status = Status.CLOSED;
    }

    public boolean isClosed() {
        return status == Status.CLOSED;
    }
}

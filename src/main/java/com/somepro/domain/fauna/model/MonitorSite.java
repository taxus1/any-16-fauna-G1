package com.somepro.domain.fauna.model;

import com.somepro.common.exception.BizException;
import com.somepro.domain.shared.model.BaseEntity;
import com.somepro.domain.shared.support.DomainChecks;
import lombok.Getter;
import lombok.Setter;

/**
 * 监测点聚合根（fauna 限界上下文）：挂在某个监测站名下的样线 / 样点 / 红外相机位。
 *
 * 纯领域对象，不带任何持久化注解。
 *
 * 不变量：
 * - 编号（siteNo）全局唯一，按 MP-2026-0001 规则由应用侧生成；
 * - 只能挂到「实实在在、且没关闭」的站上：站不存在 / 停用 / 关闭都不允许挂，
 *   「站上能不能挂」由应用层在注册时核对（聚合自身拿不到站仓储）；
 * - 类型、生境取值见 {@link SiteType} / {@link Habitat}；
 * - 新点默认在册 ACTIVE，状态只有 在册 / 停测 两档。
 */
@Getter
@Setter
public class MonitorSite extends BaseEntity {

    /** 类型：样线 / 样点 / 红外相机位。 */
    public enum SiteType {
        TRANSECT,
        SAMPLE_POINT,
        CAMERA
    }

    /** 生境：林地 / 湿地 / 草地 / 农田 / 荒漠。 */
    public enum Habitat {
        FOREST,
        WETLAND,
        GRASSLAND,
        FARMLAND,
        DESERT
    }

    /** 状态：在册 / 停测。 */
    public enum Status {
        ACTIVE,
        INACTIVE
    }

    private Long id;

    /** 监测点编号，全局唯一（如 MP-2026-0001），注册时由应用侧分配。 */
    private String siteNo;

    /** 所属监测站 id（t_monitor_station.id）。 */
    private Long stationId;

    private SiteType siteType;

    private Habitat habitat;

    /** 位置描述。 */
    private String location;

    private Status status;

    /** 工厂方法：新点默认在册。stationId 为必填，站上是否有效由应用层先行核对。 */
    public static MonitorSite register(String siteNo, Long stationId, String siteType,
                                       String habitat, String location) {
        MonitorSite site = new MonitorSite();
        site.siteNo = DomainChecks.requireText(siteNo, "监测点编号");
        if (stationId == null) {
            throw new BizException("所属监测站不能为空");
        }
        site.stationId = stationId;
        site.edit(siteType, habitat, location);
        site.status = Status.ACTIVE;
        return site;
    }

    /** 修改点位档案（类型 / 生境 / 位置说明），不碰归属与状态。 */
    public void edit(String siteType, String habitat, String location) {
        this.siteType = DomainChecks.parseEnum(SiteType.class, siteType, "点位类型");
        this.habitat = DomainChecks.parseEnum(Habitat.class, habitat, "生境");
        this.location = DomainChecks.optionalText(location);
    }

    /** 在册 → 停测。重复停测直接拒绝。 */
    public void deactivate() {
        if (status == Status.INACTIVE) {
            throw new BizException("监测点已停测，无需重复操作");
        }
        this.status = Status.INACTIVE;
    }

    /** 停测 → 恢复在册。 */
    public void activate() {
        if (status == Status.ACTIVE) {
            throw new BizException("监测点在册，无需恢复");
        }
        this.status = Status.ACTIVE;
    }
}

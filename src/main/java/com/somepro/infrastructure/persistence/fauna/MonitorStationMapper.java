package com.somepro.infrastructure.persistence.fauna;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.fauna.po.MonitorStationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 监测站 MyBatis-Plus Mapper（基础设施层），阻塞 JDBC，只可在 boundedElastic 线程调用。
 */
@Mapper
public interface MonitorStationMapper extends BaseMapper<MonitorStationPO> {

    /**
     * 取指定前缀下的最大编号（含历史上已逻辑删除的行）。
     *
     * 刻意写死物理列、不带 del_flag 条件：删过的号也必须算进流水，
     * 否则重新发出来会撞上软删行上的唯一索引。自定义 SQL 不经过 @TableLogic 改写。
     */
    @Select("SELECT MAX(station_no) FROM t_monitor_station WHERE station_no LIKE #{prefix}")
    String selectMaxStationNo(@Param("prefix") String prefix);
}

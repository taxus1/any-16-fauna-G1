package com.somepro.infrastructure.persistence.fauna;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.fauna.po.MonitorSitePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 监测点 MyBatis-Plus Mapper（基础设施层），阻塞 JDBC，只可在 boundedElastic 线程调用。
 */
@Mapper
public interface MonitorSiteMapper extends BaseMapper<MonitorSitePO> {

    /** 取指定前缀下的最大点位编号（含逻辑删除行），避免重发旧号撞唯一索引。 */
    @Select("SELECT MAX(site_no) FROM t_monitor_site WHERE site_no LIKE #{prefix}")
    String selectMaxSiteNo(@Param("prefix") String prefix);
}

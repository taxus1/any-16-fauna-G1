package com.somepro.infrastructure.persistence.fauna;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.fauna.po.SpeciesPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 物种名录 MyBatis-Plus Mapper（基础设施层），阻塞 JDBC，只可在 boundedElastic 线程调用。
 */
@Mapper
public interface SpeciesMapper extends BaseMapper<SpeciesPO> {

    /** 取 SP- 前缀下的最大物种编码（含逻辑删除行），避免重发旧码撞唯一索引。 */
    @Select("SELECT MAX(species_code) FROM t_species WHERE species_code LIKE #{prefix}")
    String selectMaxSpeciesCode(@Param("prefix") String prefix);
}

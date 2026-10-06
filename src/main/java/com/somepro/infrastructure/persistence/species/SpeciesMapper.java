package com.somepro.infrastructure.persistence.species;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.species.po.SpeciesPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 物种名录的 MyBatis-Plus Mapper（基础设施层）。
 *
 * 阻塞（JDBC）API，只能在 boundedElastic 线程上调用。
 */
@Mapper
public interface SpeciesMapper extends BaseMapper<SpeciesPO> {

    /**
     * 查指定号段内已用的最大序号（编码生成用）。
     * 自定义 @Select 不拼 del_flag 条件：已删除记录占用的编码也不复用。
     */
    @Select("SELECT MAX(CAST(SUBSTRING(species_code, #{seqStart}) AS UNSIGNED)) "
            + "FROM t_species WHERE species_code LIKE CONCAT(#{prefix}, '%')")
    Long selectMaxSeq(@Param("prefix") String prefix, @Param("seqStart") int seqStart);
}

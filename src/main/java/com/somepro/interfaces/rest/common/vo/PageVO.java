package com.somepro.interfaces.rest.common.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 对外分页返回对象（VO，用户接口层）—— 不可变 record。
 *
 * 与领域层 PageResult 的分工：PageResult 只有 4 个组件（record 只序列化组件），
 * 派生字段 totalPages 放在接口层，方便前端直接渲染分页器；领域层保持零框架依赖。
 */
public record PageVO<T>(List<T> content, long total, int pageNum, int pageSize, int totalPages)
        implements Serializable {
}

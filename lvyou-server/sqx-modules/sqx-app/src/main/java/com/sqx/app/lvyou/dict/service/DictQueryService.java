package com.sqx.app.lvyou.dict.service;

import com.sqx.app.lvyou.dict.domain.vo.DictItemVo;

import java.util.List;

/**
 * 业务字典查询
 */
public interface DictQueryService {

    /**
     * 按 type 查询字典项，空库时回退内置枚举
     */
    List<DictItemVo> listByType(String type);

    /**
     * 解析展示文案
     */
    String labelOf(String type, String code);
}

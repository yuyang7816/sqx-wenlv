package com.sqx.app.lvyou.search.service;

import com.sqx.app.lvyou.search.domain.vo.SearchResultVo;

/**
 * 综合搜索服务
 */
public interface SearchService {

    /**
     * 跨模块关键词搜索，每类最多返回5条
     *
     * @param keywordCode     搜索关键词编码（必填，仅 ASCII）
     * @param destinationId   目的地数据库主键（可选，由 destinationId 业务编码解析）
     */
    SearchResultVo search(String keywordCode, Long destinationId);
}

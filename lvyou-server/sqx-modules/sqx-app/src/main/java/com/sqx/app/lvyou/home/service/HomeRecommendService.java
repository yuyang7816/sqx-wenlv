package com.sqx.app.lvyou.home.service;

import com.sqx.app.lvyou.home.domain.vo.HomeRecommendVo;

/**
 * 首页推荐聚合服务
 */
public interface HomeRecommendService {

    /**
     * 按目的地聚合酒店/线路/活动/景区推荐
     */
    HomeRecommendVo recommend(Long destinationId);
}

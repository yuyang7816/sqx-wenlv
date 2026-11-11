package com.sqx.app.lvyou.home.service;

import com.sqx.app.lvyou.home.domain.vo.HomeRecommendItemVo;

import java.util.List;

/**
 * 首页推荐中的商户服务内容
 */
public interface MerchantHomeRecommendService {

    /**
     * 按目的地与 Tab 键拉取上架中的商户服务
     */
    List<HomeRecommendItemVo> listByTab(Long destinationId, String tabKey, int limit);
}

package com.sqx.app.lvyou.route.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.home.domain.vo.HomeRecommendItemVo;
import com.sqx.app.lvyou.route.domain.bo.*;
import com.sqx.app.lvyou.heritage.goods.domain.vo.ReviewAppVo;
import com.sqx.app.lvyou.route.domain.vo.RouteAppDetailVo;
import com.sqx.app.lvyou.route.domain.vo.RouteAppVo;
import com.sqx.app.lvyou.route.domain.vo.RouteItineraryPageVo;
import com.sqx.app.lvyou.route.domain.vo.RouteVo;

import java.util.List;

/**
 * 旅游线路业务服务
 */
public interface RouteService {

    /**
     * 查询首页推荐线路
     */
    List<HomeRecommendItemVo> listRecommend(Long destinationId, int limit);

    /**
     * 用户端分页列表（仅上架）
     */
    IPage<RouteAppVo> appPage(RouteAppListBo listBo);

    /**
     * 用户端详情（含行程预览）
     */
    RouteAppDetailVo appDetail(Long id, Long userId);

    /**
     * 用户端完整行程
     */
    RouteItineraryPageVo appItinerary(Long id);

    /**
     * 管理端分页列表
     */
    IPage<RouteVo> pageInfo(RoutePageBo pageBo);

    /**
     * 管理端新增
     */
    void saveInfo(RouteSaveBo saveBo);

    /**
     * 管理端更新
     */
    void updateInfo(RouteUpdateBo updateBo);

    /**
     * 管理端删除
     */
    void delete(RouteInfoBo infoBo);

    /**
     * 管理端更新状态
     */
    void updateState(RouteUpdateStateBo updateStateBo);

    /**
     * 旅行社门店评价列表
     */
    IPage<ReviewAppVo> appStoreReviews(RouteReviewAppListBo listBo);

    /**
     * 提交旅行社门店评价
     */
    ReviewAppVo submitStoreReview(Long userId, Long merchantId, RouteReviewSubmitBo submitBo);
}

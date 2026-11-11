package com.sqx.app.lvyou.activity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.activity.domain.bo.*;
import com.sqx.app.lvyou.activity.domain.vo.*;
import com.sqx.app.lvyou.home.domain.vo.HomeRecommendItemVo;

import java.util.List;

/**
 * 精彩活动业务服务
 */
public interface ActivityService {

    /**
     * 查询首页推荐活动
     */
    List<HomeRecommendItemVo> listRecommend(Long destinationId, int limit);

    /**
     * 用户端活动分页列表
     */
    IPage<ActivityAppVo> appPage(ActivityAppListBo listBo);

    /**
     * 用户端活动详情
     */
    ActivityAppDetailVo appDetail(Long id, Long userId);

    /**
     * 用户端活动报名
     */
    ActivityEnrollVo enroll(Long activityId, Long userId);

    /**
     * 管理端分页列表
     */
    IPage<ActivityVo> pageInfo(ActivityPageBo pageBo);

    /**
     * 管理端新增
     */
    void saveInfo(ActivitySaveBo saveBo);

    /**
     * 管理端更新
     */
    void updateInfo(ActivityUpdateBo updateBo);

    /**
     * 管理端删除
     */
    void delete(ActivityInfoBo infoBo);

    /**
     * 管理端上下架
     */
    void updateState(ActivityUpdateStateBo updateStateBo);
}

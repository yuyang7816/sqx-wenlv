package com.sqx.app.lvyou.scenic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.home.domain.vo.HomeRecommendItemVo;
import com.sqx.app.lvyou.scenic.domain.bo.*;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicAppDetailVo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicAppVo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicVo;

import java.util.List;

/**
 * 旅游景区业务服务
 */
public interface ScenicService {

    /**
     * 查询首页推荐景区
     */
    List<HomeRecommendItemVo> listRecommend(Long destinationId, int limit);

    /**
     * 用户端景区分页列表
     */
    IPage<ScenicAppVo> appPage(ScenicAppListBo listBo);

    /**
     * 用户端景区详情
     */
    ScenicAppDetailVo appDetail(Long id, Long userId);

    /**
     * 管理端分页列表
     */
    IPage<ScenicVo> pageInfo(ScenicPageBo pageBo);

    /**
     * 管理端新增
     */
    void saveInfo(ScenicSaveBo saveBo);

    /**
     * 管理端更新
     */
    void updateInfo(ScenicUpdateBo updateBo);

    /**
     * 管理端删除
     */
    void delete(ScenicInfoBo infoBo);

    /**
     * 管理端上下架
     */
    void updateState(ScenicUpdateStateBo updateStateBo);
}

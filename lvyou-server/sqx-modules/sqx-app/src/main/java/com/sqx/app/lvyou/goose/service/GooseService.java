package com.sqx.app.lvyou.goose.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.goose.domain.bo.GooseAppListBo;
import com.sqx.app.lvyou.goose.domain.vo.GooseAppDetailVo;
import com.sqx.app.lvyou.goose.domain.vo.GooseAppVo;
import com.sqx.app.lvyou.goose.domain.vo.GooseDishDetailVo;
import com.sqx.app.lvyou.goose.domain.vo.GooseMenuItemVo;

import java.util.List;

/**
 * 灰鹅美食业务服务
 */
public interface GooseService {

    /**
     * 商家分页列表（仅正常营业的 goose/restaurant 商户）
     */
    IPage<GooseAppVo> appPage(GooseAppListBo listBo);

    /**
     * 商家详情（含招牌菜预览、营业时间）
     */
    GooseAppDetailVo appDetail(Long id, Long userId);

    /**
     * 全部在售菜单
     */
    List<GooseMenuItemVo> appMenu(Long id);

    /**
     * 菜品详情（含轮播图）
     */
    GooseDishDetailVo appDishDetail(Long serviceId);
}

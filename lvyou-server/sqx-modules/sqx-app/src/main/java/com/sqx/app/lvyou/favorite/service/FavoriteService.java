package com.sqx.app.lvyou.favorite.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.favorite.domain.bo.FavoriteToggleBo;
import com.sqx.app.lvyou.favorite.domain.vo.AssetItemVo;
import com.sqx.app.lvyou.favorite.domain.vo.FavoriteToggleVo;

/**
 * 用户收藏业务服务
 */
public interface FavoriteService {

    IPage<AssetItemVo> list(Long userId, String targetType, Long page, Long limit);

    FavoriteToggleVo toggle(Long userId, FavoriteToggleBo toggleBo);
}

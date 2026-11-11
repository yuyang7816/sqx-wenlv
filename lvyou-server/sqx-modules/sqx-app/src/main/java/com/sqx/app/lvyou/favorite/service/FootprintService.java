package com.sqx.app.lvyou.favorite.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.favorite.domain.vo.AssetItemVo;

/**
 * 用户浏览足迹业务服务
 */
public interface FootprintService {

    IPage<AssetItemVo> list(Long userId, String targetType, Long page, Long limit);

    void recordAsync(Long userId, String targetType, Long targetId);
}

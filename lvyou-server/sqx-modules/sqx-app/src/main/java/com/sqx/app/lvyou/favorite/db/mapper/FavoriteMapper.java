package com.sqx.app.lvyou.favorite.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.lvyou.favorite.domain.entity.FavoriteEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收藏 Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<FavoriteEntity> {
}

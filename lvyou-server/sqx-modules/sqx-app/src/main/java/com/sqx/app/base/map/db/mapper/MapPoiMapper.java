package com.sqx.app.base.map.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.base.map.domain.entity.MapPoiEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 地图 POI Mapper
 */
@Mapper
public interface MapPoiMapper extends BaseMapper<MapPoiEntity> {
}

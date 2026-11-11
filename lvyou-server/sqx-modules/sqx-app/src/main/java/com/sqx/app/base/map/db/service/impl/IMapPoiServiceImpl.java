package com.sqx.app.base.map.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.base.map.db.mapper.MapPoiMapper;
import com.sqx.app.base.map.db.service.IMapPoiService;
import com.sqx.app.base.map.domain.entity.MapPoiEntity;
import org.springframework.stereotype.Service;

/**
 * 地图 POI 数据服务实现
 */
@Service
public class IMapPoiServiceImpl extends ServiceImpl<MapPoiMapper, MapPoiEntity> implements IMapPoiService {
}

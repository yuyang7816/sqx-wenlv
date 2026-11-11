package com.sqx.app.lvyou.transport.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.lvyou.transport.domain.entity.TransportRouteEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公交线路 Mapper
 */
@Mapper
public interface TransportRouteMapper extends BaseMapper<TransportRouteEntity> {
}

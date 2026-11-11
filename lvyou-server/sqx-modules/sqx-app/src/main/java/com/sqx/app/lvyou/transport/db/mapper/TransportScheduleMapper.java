package com.sqx.app.lvyou.transport.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.lvyou.transport.domain.entity.TransportScheduleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公交时刻表 Mapper
 */
@Mapper
public interface TransportScheduleMapper extends BaseMapper<TransportScheduleEntity> {
}

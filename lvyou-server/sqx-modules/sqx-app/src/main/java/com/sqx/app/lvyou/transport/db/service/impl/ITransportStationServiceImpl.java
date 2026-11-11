package com.sqx.app.lvyou.transport.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.transport.db.mapper.TransportStationMapper;
import com.sqx.app.lvyou.transport.db.service.ITransportStationService;
import com.sqx.app.lvyou.transport.domain.entity.TransportStationEntity;
import org.springframework.stereotype.Service;

/**
 * 公交站点数据服务实现
 */
@Service
public class ITransportStationServiceImpl extends ServiceImpl<TransportStationMapper, TransportStationEntity>
        implements ITransportStationService {
}

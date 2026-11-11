package com.sqx.app.lvyou.transport.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.transport.db.mapper.TransportRouteMapper;
import com.sqx.app.lvyou.transport.db.service.ITransportRouteService;
import com.sqx.app.lvyou.transport.domain.entity.TransportRouteEntity;
import org.springframework.stereotype.Service;

/**
 * 公交线路数据服务实现
 */
@Service
public class ITransportRouteServiceImpl extends ServiceImpl<TransportRouteMapper, TransportRouteEntity>
        implements ITransportRouteService {
}

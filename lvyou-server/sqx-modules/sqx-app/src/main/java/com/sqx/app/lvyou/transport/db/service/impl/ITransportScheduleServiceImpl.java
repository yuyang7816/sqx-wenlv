package com.sqx.app.lvyou.transport.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.transport.db.mapper.TransportScheduleMapper;
import com.sqx.app.lvyou.transport.db.service.ITransportScheduleService;
import com.sqx.app.lvyou.transport.domain.entity.TransportScheduleEntity;
import org.springframework.stereotype.Service;

/**
 * 公交时刻表数据服务实现
 */
@Service
public class ITransportScheduleServiceImpl extends ServiceImpl<TransportScheduleMapper, TransportScheduleEntity>
        implements ITransportScheduleService {
}

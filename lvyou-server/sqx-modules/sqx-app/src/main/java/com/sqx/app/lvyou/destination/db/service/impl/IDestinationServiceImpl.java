package com.sqx.app.lvyou.destination.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.destination.db.mapper.DestinationMapper;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import org.springframework.stereotype.Service;

/**
 * 目的地数据服务实现
 */
@Service
public class IDestinationServiceImpl extends ServiceImpl<DestinationMapper, DestinationEntity> implements IDestinationService {
}

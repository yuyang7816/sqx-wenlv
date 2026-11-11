package com.sqx.app.lvyou.invite.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.invite.db.mapper.CommissionRecordMapper;
import com.sqx.app.lvyou.invite.db.service.ICommissionRecordService;
import com.sqx.app.lvyou.invite.domain.entity.CommissionRecordEntity;
import org.springframework.stereotype.Service;

/**
 * 邀请佣金记录数据服务实现
 */
@Service
public class ICommissionRecordServiceImpl extends ServiceImpl<CommissionRecordMapper, CommissionRecordEntity>
        implements ICommissionRecordService {
}

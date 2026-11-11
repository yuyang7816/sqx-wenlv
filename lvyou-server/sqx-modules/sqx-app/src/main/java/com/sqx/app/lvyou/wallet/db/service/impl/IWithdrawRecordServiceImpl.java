package com.sqx.app.lvyou.wallet.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.wallet.db.mapper.WithdrawRecordMapper;
import com.sqx.app.lvyou.wallet.db.service.IWithdrawRecordService;
import com.sqx.app.lvyou.wallet.domain.entity.WithdrawRecordEntity;
import org.springframework.stereotype.Service;

/**
 * 提现记录 DB 服务实现
 */
@Service
public class IWithdrawRecordServiceImpl extends ServiceImpl<WithdrawRecordMapper, WithdrawRecordEntity>
        implements IWithdrawRecordService {
}

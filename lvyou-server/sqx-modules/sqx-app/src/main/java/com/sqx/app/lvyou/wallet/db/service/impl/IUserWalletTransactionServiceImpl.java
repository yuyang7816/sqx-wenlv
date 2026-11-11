package com.sqx.app.lvyou.wallet.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.wallet.db.mapper.UserWalletTransactionMapper;
import com.sqx.app.lvyou.wallet.db.service.IUserWalletTransactionService;
import com.sqx.app.lvyou.wallet.domain.entity.UserWalletTransactionEntity;
import org.springframework.stereotype.Service;

/**
 * 用户钱包流水 DB 服务实现
 */
@Service
public class IUserWalletTransactionServiceImpl extends ServiceImpl<UserWalletTransactionMapper, UserWalletTransactionEntity> implements IUserWalletTransactionService {
}

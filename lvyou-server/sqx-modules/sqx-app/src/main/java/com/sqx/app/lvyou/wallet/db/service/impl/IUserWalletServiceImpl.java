package com.sqx.app.lvyou.wallet.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.wallet.db.mapper.UserWalletMapper;
import com.sqx.app.lvyou.wallet.db.service.IUserWalletService;
import com.sqx.app.lvyou.wallet.domain.entity.UserWalletEntity;
import org.springframework.stereotype.Service;

/**
 * 用户钱包 DB 服务实现
 */
@Service
public class IUserWalletServiceImpl extends ServiceImpl<UserWalletMapper, UserWalletEntity> implements IUserWalletService {
}

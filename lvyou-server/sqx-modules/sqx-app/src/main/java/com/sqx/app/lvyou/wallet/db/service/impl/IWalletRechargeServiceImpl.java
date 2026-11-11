package com.sqx.app.lvyou.wallet.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.wallet.db.mapper.WalletRechargeMapper;
import com.sqx.app.lvyou.wallet.db.service.IWalletRechargeService;
import com.sqx.app.lvyou.wallet.domain.entity.WalletRechargeEntity;
import org.springframework.stereotype.Service;

/**
 * 钱包充值单 DB 服务实现
 */
@Service
public class IWalletRechargeServiceImpl extends ServiceImpl<WalletRechargeMapper, WalletRechargeEntity> implements IWalletRechargeService {
}

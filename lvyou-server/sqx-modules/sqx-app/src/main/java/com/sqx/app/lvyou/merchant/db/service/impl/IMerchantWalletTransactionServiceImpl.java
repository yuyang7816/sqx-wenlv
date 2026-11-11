package com.sqx.app.lvyou.merchant.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.merchant.db.mapper.MerchantWalletTransactionMapper;
import com.sqx.app.lvyou.merchant.db.service.IMerchantWalletTransactionService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantWalletTransactionEntity;
import org.springframework.stereotype.Service;

/**
 * 商户钱包流水 DB 服务实现
 */
@Service
public class IMerchantWalletTransactionServiceImpl
        extends ServiceImpl<MerchantWalletTransactionMapper, MerchantWalletTransactionEntity>
        implements IMerchantWalletTransactionService {
}

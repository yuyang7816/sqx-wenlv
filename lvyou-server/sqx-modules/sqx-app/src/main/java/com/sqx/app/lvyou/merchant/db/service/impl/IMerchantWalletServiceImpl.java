package com.sqx.app.lvyou.merchant.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.merchant.db.mapper.MerchantWalletMapper;
import com.sqx.app.lvyou.merchant.db.service.IMerchantWalletService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantWalletEntity;
import org.springframework.stereotype.Service;

/**
 * 商户钱包 DB 服务实现
 */
@Service
public class IMerchantWalletServiceImpl extends ServiceImpl<MerchantWalletMapper, MerchantWalletEntity>
        implements IMerchantWalletService {
}

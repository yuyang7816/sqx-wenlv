package com.sqx.app.lvyou.merchant.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.merchant.db.mapper.MerchantDepositRefundMapper;
import com.sqx.app.lvyou.merchant.db.service.IMerchantDepositRefundService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantDepositRefundEntity;
import org.springframework.stereotype.Service;

@Service
public class IMerchantDepositRefundServiceImpl
        extends ServiceImpl<MerchantDepositRefundMapper, MerchantDepositRefundEntity>
        implements IMerchantDepositRefundService {
}

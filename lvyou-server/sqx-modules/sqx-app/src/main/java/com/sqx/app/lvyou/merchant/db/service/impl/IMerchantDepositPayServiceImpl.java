package com.sqx.app.lvyou.merchant.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.merchant.db.mapper.MerchantDepositPayMapper;
import com.sqx.app.lvyou.merchant.db.service.IMerchantDepositPayService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantDepositPayEntity;
import org.springframework.stereotype.Service;

@Service
public class IMerchantDepositPayServiceImpl
        extends ServiceImpl<MerchantDepositPayMapper, MerchantDepositPayEntity>
        implements IMerchantDepositPayService {
}

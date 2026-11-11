package com.sqx.app.lvyou.merchant.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.merchant.db.mapper.MerchantMapper;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import org.springframework.stereotype.Service;

/**
 * 商户数据服务实现
 */
@Service
public class IMerchantServiceImpl extends ServiceImpl<MerchantMapper, MerchantEntity> implements IMerchantService {
}

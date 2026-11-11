package com.sqx.app.lvyou.merchant.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.merchant.db.mapper.MerchantStoreDecorMapper;
import com.sqx.app.lvyou.merchant.db.service.IMerchantStoreDecorService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantStoreDecorEntity;
import org.springframework.stereotype.Service;

/**
 * 商户门店装修 DB 服务实现
 */
@Service
public class IMerchantStoreDecorServiceImpl extends ServiceImpl<MerchantStoreDecorMapper, MerchantStoreDecorEntity>
        implements IMerchantStoreDecorService {
}

package com.sqx.app.lvyou.merchant.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.merchant.db.mapper.MerchantServiceItemMapper;
import com.sqx.app.lvyou.merchant.db.service.IMerchantServiceItemService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantServiceEntity;
import org.springframework.stereotype.Service;

/**
 * 商户服务 DB 服务实现
 */
@Service
public class IMerchantServiceItemServiceImpl extends ServiceImpl<MerchantServiceItemMapper, MerchantServiceEntity>
        implements IMerchantServiceItemService {
}

package com.sqx.app.lvyou.merchant.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.merchant.db.mapper.MerchantApplyMapper;
import com.sqx.app.lvyou.merchant.db.service.IMerchantApplyService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantApplyEntity;
import org.springframework.stereotype.Service;

/**
 * 商户入驻申请数据服务实现
 */
@Service
public class IMerchantApplyServiceImpl extends ServiceImpl<MerchantApplyMapper, MerchantApplyEntity> implements IMerchantApplyService {
}

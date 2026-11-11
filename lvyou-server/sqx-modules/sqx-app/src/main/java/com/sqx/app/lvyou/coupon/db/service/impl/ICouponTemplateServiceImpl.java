package com.sqx.app.lvyou.coupon.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.coupon.db.mapper.CouponTemplateMapper;
import com.sqx.app.lvyou.coupon.db.service.ICouponTemplateService;
import com.sqx.app.lvyou.coupon.domain.entity.CouponTemplateEntity;
import org.springframework.stereotype.Service;

/**
 * 优惠券模板数据服务实现
 */
@Service
public class ICouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplateEntity>
        implements ICouponTemplateService {
}

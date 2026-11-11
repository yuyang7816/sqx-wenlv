package com.sqx.app.lvyou.coupon.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.coupon.db.mapper.UserCouponMapper;
import com.sqx.app.lvyou.coupon.db.service.IUserCouponService;
import com.sqx.app.lvyou.coupon.domain.entity.UserCouponEntity;
import org.springframework.stereotype.Service;

/**
 * 用户优惠券数据服务实现
 */
@Service
public class IUserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCouponEntity>
        implements IUserCouponService {
}

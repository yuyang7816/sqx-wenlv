package com.sqx.app.lvyou.coupon.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.coupon.domain.bo.CouponClaimBo;
import com.sqx.app.lvyou.coupon.domain.bo.CouponTemplateInfoBo;
import com.sqx.app.lvyou.coupon.domain.bo.CouponTemplatePageBo;
import com.sqx.app.lvyou.coupon.domain.bo.CouponTemplateSaveBo;
import com.sqx.app.lvyou.coupon.domain.bo.CouponTemplateUpdateBo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponAvailableVo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponCenterVo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponCountVo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponMineVo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponTemplateVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券业务服务
 */
public interface CouponService {

    IPage<CouponTemplateVo> templatePage(CouponTemplatePageBo pageBo);

    void templateSave(CouponTemplateSaveBo saveBo);

    void templateUpdate(CouponTemplateUpdateBo updateBo);

    void templateDelete(CouponTemplateInfoBo infoBo);

    List<CouponCenterVo> center(Long userId);

    CouponMineVo claim(Long userId, CouponClaimBo claimBo);

    List<CouponMineVo> mine(Long userId, String status);

    List<CouponAvailableVo> available(Long userId, String scope, BigDecimal amount);

    CouponCountVo countAvailable(Long userId);
}

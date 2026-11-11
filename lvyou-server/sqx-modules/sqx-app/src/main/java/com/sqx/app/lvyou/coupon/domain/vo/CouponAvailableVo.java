package com.sqx.app.lvyou.coupon.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 下单可用优惠券
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "下单可用优惠券")
public class CouponAvailableVo extends CouponMineVo {

    @Schema(description = "是否满足当前订单条件")
    private Boolean applicable;

    @Schema(description = "可抵扣金额")
    private BigDecimal discount;
}

package com.sqx.app.lvyou.coupon.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 可用优惠券数量
 */
@Data
@Schema(description = "可用优惠券数量")
public class CouponCountVo {

    @Schema(description = "未使用且未过期数量")
    private Integer count;
}

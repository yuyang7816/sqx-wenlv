package com.sqx.app.lvyou.coupon.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 我的优惠券
 */
@Data
@Schema(description = "我的优惠券")
public class CouponMineVo {

    @Schema(description = "用户券ID")
    private Long id;

    @Schema(description = "模板业务编码")
    private String templateId;

    @Schema(description = "优惠券名称")
    private String name;

    @Schema(description = "优惠金额")
    private BigDecimal amount;

    @Schema(description = "最低消费门槛")
    private BigDecimal minAmount;

    @Schema(description = "适用范围：all/hotel/route/goods")
    private String scope;

    @Schema(description = "生效日期 yyyy-MM-dd")
    private String validFrom;

    @Schema(description = "失效日期 yyyy-MM-dd")
    private String validTo;

    @Schema(description = "使用说明")
    private String desc;

    @Schema(description = "状态：available/used/expired")
    private String status;
}

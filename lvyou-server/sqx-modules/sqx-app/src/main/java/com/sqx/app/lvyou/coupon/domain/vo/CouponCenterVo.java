package com.sqx.app.lvyou.coupon.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 领券中心条目
 */
@Data
@Schema(description = "领券中心条目")
public class CouponCenterVo {

    @Schema(description = "模板业务编码（前端 templateId）")
    private String templateId;

    @Schema(description = "优惠券名称")
    private String name;

    @Schema(description = "优惠金额")
    private BigDecimal amount;

    @Schema(description = "最低消费门槛")
    private BigDecimal minAmount;

    @Schema(description = "适用范围：all/hotel/route/goods")
    private String scope;

    @Schema(description = "领取后有效天数")
    private Integer validDays;

    @Schema(description = "使用说明")
    private String desc;

    @Schema(description = "当前用户是否已领取")
    private Boolean claimed;
}

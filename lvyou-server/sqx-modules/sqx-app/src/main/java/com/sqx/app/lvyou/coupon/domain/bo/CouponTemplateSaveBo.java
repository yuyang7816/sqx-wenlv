package com.sqx.app.lvyou.coupon.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 管理端新增优惠券模板
 */
@Data
@Schema(description = "新增优惠券模板")
public class CouponTemplateSaveBo {

    @Schema(description = "模板业务编码")
    @NotBlank(message = "模板业务编码不能为空")
    private String templateCode;

    @Schema(description = "优惠券名称")
    @NotBlank(message = "优惠券名称不能为空")
    private String name;

    @Schema(description = "优惠金额")
    @NotNull(message = "优惠金额不能为空")
    @DecimalMin(value = "0.01", message = "优惠金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "最低消费门槛")
    @NotNull(message = "最低消费门槛不能为空")
    @DecimalMin(value = "0", message = "最低消费门槛不能为负")
    private BigDecimal minAmount;

    @Schema(description = "适用范围：all/hotel/route/goods")
    @NotBlank(message = "适用范围不能为空")
    private String scope;

    @Schema(description = "领取后有效天数")
    @NotNull(message = "有效天数不能为空")
    @Min(value = 1, message = "有效天数至少为1天")
    private Integer validDays;

    @Schema(description = "使用说明")
    private String description;

    @Schema(description = "发放总量，0不限")
    @NotNull(message = "发放总量不能为空")
    @Min(value = 0, message = "发放总量不能为负")
    private Integer totalCount;

    @Schema(description = "每人限领张数")
    @NotNull(message = "每人限领张数不能为空")
    @Min(value = 1, message = "每人限领至少1张")
    private Integer perUserLimit;

    @Schema(description = "状态：1可领取 0下架")
    @NotNull(message = "状态不能为空")
    private Integer status;
}

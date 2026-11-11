package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 商户订单操作入参
 */
@Data
@Schema(description = "商户订单操作")
public class MerchantOrderActionBo {

    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作：confirm/ship/complete/cancel/reject/refund_agree")
    private String action;

    @Schema(description = "物流公司（发货）")
    private String logisticsCompany;

    @Schema(description = "物流单号（发货）")
    private String logisticsNo;

    @Schema(description = "取消/拒绝原因")
    private String reason;
}

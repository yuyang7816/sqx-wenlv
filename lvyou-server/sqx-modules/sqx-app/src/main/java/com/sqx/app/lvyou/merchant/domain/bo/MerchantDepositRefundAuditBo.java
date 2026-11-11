package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "商户退保审核")
public class MerchantDepositRefundAuditBo {

    @NotNull(message = "商户ID不能为空")
    @Schema(description = "商户ID")
    private Long merchantId;

    @NotBlank(message = "退保单号不能为空")
    @Schema(description = "退保单号")
    private String refundNo;

    /** approve / reject */
    @NotBlank(message = "审核动作不能为空")
    @Schema(description = "审核动作：approve/reject")
    private String action;

    @Schema(description = "驳回原因（驳回时必填，会展示给商户）")
    private String remark;
}

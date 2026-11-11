package com.sqx.app.lvyou.merchant.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "商户退保申请")
public class MerchantDepositRefundVo {

    @Schema(description = "退保单号")
    private String refundNo;

    @Schema(description = "申请金额")
    private BigDecimal amount;

    @Schema(description = "状态：pending/approved/rejected")
    private String status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "申请原因")
    private String applyReason;

    @Schema(description = "审核备注（驳回原因，会展示给商户）")
    private String auditRemark;

    @Schema(description = "申请时间")
    private String createTime;

    @Schema(description = "审核时间")
    private String auditTime;
}

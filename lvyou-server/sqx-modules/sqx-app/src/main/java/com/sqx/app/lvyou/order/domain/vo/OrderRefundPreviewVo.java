package com.sqx.app.lvyou.order.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "线路退款预览")
public class OrderRefundPreviewVo {

    @Schema(description = "订单实付")
    private BigDecimal payAmount;

    @Schema(description = "适用规则说明")
    private String ruleLabel;

    @Schema(description = "扣款比例 0-100（从实付中扣留，余款退用户）")
    private Integer refundRatio;

    @Schema(description = "退还用户比例 0-100（100 - 扣款比例）")
    private Integer userRefundRatio;

    @Schema(description = "平台抽成比例 0-100（仅管理端预览）")
    private Integer commissionRate;

    @Schema(description = "预计退用户金额")
    private BigDecimal userRefund;

    @Schema(description = "平台服务费（仅管理端预览，从扣留金额中扣除）")
    private BigDecimal platformFee;

    @Schema(description = "商户预计入账（仅管理端预览）")
    private BigDecimal merchantIncome;

    @Schema(description = "退改规则摘要")
    private String policySummary;
}

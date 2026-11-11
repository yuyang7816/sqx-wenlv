package com.sqx.app.base.pay.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端支付流水 VO
 */
@Data
@Schema(description = "管理端支付流水")
public class PaymentAdminVo {

    @Schema(description = "支付记录ID")
    private Long id;

    @Schema(description = "支付单号（订单号或充值单号）")
    private String payNo;

    @Schema(description = "业务类型：tourism/wallet_recharge")
    private String businessType;

    @Schema(description = "业务类型中文")
    private String businessTypeLabel;

    @Schema(description = "支付方式：wx/ali/wallet")
    private String payMethod;

    @Schema(description = "支付方式中文")
    private String payMethodLabel;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "用户手机号")
    private String userPhone;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "第三方交易号")
    private String tradeNo;

    @Schema(description = "支付时间")
    private String payTime;

    @Schema(description = "订单分类（文旅订单）")
    private String orderCategory;

    @Schema(description = "订单分类中文")
    private String orderCategoryLabel;

    @Schema(description = "商户名称（文旅订单）")
    private String merchantName;

    @Schema(description = "关联订单ID（文旅订单）")
    private Long orderId;
}

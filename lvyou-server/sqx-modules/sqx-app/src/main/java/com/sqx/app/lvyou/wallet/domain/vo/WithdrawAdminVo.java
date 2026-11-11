package com.sqx.app.lvyou.wallet.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端提现记录
 */
@Data
public class WithdrawAdminVo {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "提现单号")
    private String withdrawNo;

    @Schema(description = "主体类型：user/merchant")
    private String ownerType;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户手机号")
    private String userPhone;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "提现金额")
    private BigDecimal amount;

    @Schema(description = "手续费")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账金额")
    private BigDecimal actualAmount;

    @Schema(description = "提现方式：alipay/wechat")
    private String method;

    @Schema(description = "到账账户（脱敏）")
    private String account;

    @Schema(description = "收款姓名")
    private String realName;

    @Schema(description = "微信收款码URL")
    private String qrCodeUrl;

    @Schema(description = "状态：pending/success/failed")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "申请时间")
    private String createTime;

    @Schema(description = "审核时间")
    private String auditTime;
}

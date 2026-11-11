package com.sqx.app.lvyou.wallet.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端提现记录
 */
@Data
public class WalletWithdrawRecordVo {

    @Schema(description = "提现单号")
    private String id;

    @Schema(description = "提现方式：alipay/wechat")
    private String method;

    @Schema(description = "方式展示名")
    private String methodLabel;

    @Schema(description = "提现金额")
    private BigDecimal amount;

    @Schema(description = "手续费")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账金额")
    private BigDecimal actualAmount;

    @Schema(description = "到账账户（脱敏）")
    private String account;

    @Schema(description = "收款姓名")
    private String realName;

    @Schema(description = "微信收款码URL")
    private String qrCode;

    @Schema(description = "申请时间")
    private String time;

    @Schema(description = "状态：pending/success/failed")
    private String status;

    @Schema(description = "备注")
    private String remark;
}

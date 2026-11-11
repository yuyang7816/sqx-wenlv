package com.sqx.app.lvyou.wallet.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现记录
 */
@Getter
@Setter
@TableName("tb_withdraw_record")
@Schema(name = "WithdrawRecordEntity", description = "提现记录")
public class WithdrawRecordEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "提现单号")
    @TableField("withdraw_no")
    private String withdrawNo;

    @Schema(description = "主体类型：user/merchant")
    @TableField("owner_type")
    private String ownerType;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "商户ID")
    @TableField("merchant_id")
    private Long merchantId;

    @Schema(description = "提现金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "手续费")
    @TableField("fee_amount")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账金额")
    @TableField("actual_amount")
    private BigDecimal actualAmount;

    @Schema(description = "提现方式：alipay/wechat")
    @TableField("method")
    private String method;

    @Schema(description = "到账账户（脱敏）")
    @TableField("account")
    private String account;

    @Schema(description = "到账账户原文")
    @TableField("account_raw")
    private String accountRaw;

    @Schema(description = "收款姓名")
    @TableField("real_name")
    private String realName;

    @Schema(description = "微信收款码URL")
    @TableField("qr_code_url")
    private String qrCodeUrl;

    @Schema(description = "状态：pending/success/failed")
    @TableField("status")
    private String status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("audit_time")
    private LocalDateTime auditTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

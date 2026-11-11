package com.sqx.app.lvyou.merchant.domain.entity;

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
 * 商户钱包
 */
@Getter
@Setter
@TableName("tb_merchant_wallet")
@Schema(name = "MerchantWalletEntity", description = "商户钱包")
public class MerchantWalletEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "商户ID")
    @TableField("merchant_id")
    private Long merchantId;

    @Schema(description = "可提现余额")
    @TableField("balance")
    private BigDecimal balance;

    @Schema(description = "待结算金额")
    @TableField("pending_settle")
    private BigDecimal pendingSettle;

    @Schema(description = "累计收入")
    @TableField("total_income")
    private BigDecimal totalIncome;

    @Schema(description = "应缴保证金")
    @TableField("deposit_required")
    private BigDecimal depositRequired;

    @Schema(description = "已缴保证金")
    @TableField("deposit_paid")
    private BigDecimal depositPaid;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

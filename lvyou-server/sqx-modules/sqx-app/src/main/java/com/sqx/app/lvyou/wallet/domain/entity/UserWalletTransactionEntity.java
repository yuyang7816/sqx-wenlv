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
 * 用户钱包流水
 */
@Getter
@Setter
@TableName("tb_user_wallet_transaction")
@Schema(name = "UserWalletTransactionEntity", description = "用户钱包流水")
public class UserWalletTransactionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "流水号")
    @TableField("tx_no")
    private String txNo;

    @Schema(description = "流水类型")
    @TableField("tx_type")
    private String txType;

    @Schema(description = "流水标题")
    @TableField("title")
    private String title;

    @Schema(description = "变动金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "变动后余额")
    @TableField("balance_after")
    private BigDecimal balanceAfter;

    @Schema(description = "关联业务单号")
    @TableField("ref_no")
    private String refNo;

    @Schema(description = "状态：1成功 0处理中 2失败")
    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

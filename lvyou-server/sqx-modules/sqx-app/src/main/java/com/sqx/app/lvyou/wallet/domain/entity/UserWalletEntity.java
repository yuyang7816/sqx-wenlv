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
 * 用户钱包
 */
@Getter
@Setter
@TableName("tb_user_wallet")
@Schema(name = "UserWalletEntity", description = "用户钱包")
public class UserWalletEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "账户余额")
    @TableField("balance")
    private BigDecimal balance;

    @Schema(description = "累计充值")
    @TableField("total_recharge")
    private BigDecimal totalRecharge;

    @Schema(description = "累计消费")
    @TableField("total_consume")
    private BigDecimal totalConsume;

    @Schema(description = "累计提现")
    @TableField("total_withdraw")
    private BigDecimal totalWithdraw;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

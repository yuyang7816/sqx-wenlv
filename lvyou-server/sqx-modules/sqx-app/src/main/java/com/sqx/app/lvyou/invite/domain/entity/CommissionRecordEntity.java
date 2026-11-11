package com.sqx.app.lvyou.invite.domain.entity;

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
 * 邀请佣金记录
 */
@Getter
@Setter
@TableName("tb_commission_record")
@Schema(name = "CommissionRecordEntity", description = "邀请佣金记录")
public class CommissionRecordEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "受益人用户ID")
    @TableField("beneficiary_user_id")
    private Long beneficiaryUserId;

    @Schema(description = "下单用户ID")
    @TableField("source_user_id")
    private Long sourceUserId;

    @Schema(description = "订单ID")
    @TableField("order_id")
    private Long orderId;

    @Schema(description = "订单号")
    @TableField("order_no")
    private String orderNo;

    @Schema(description = "佣金层级：1一级 2二级")
    @TableField("commission_level")
    private Integer commissionLevel;

    @Schema(description = "订单实付金额")
    @TableField("order_amount")
    private BigDecimal orderAmount;

    @Schema(description = "佣金比例")
    @TableField("commission_rate")
    private BigDecimal commissionRate;

    @Schema(description = "佣金金额")
    @TableField("commission_amount")
    private BigDecimal commissionAmount;

    @Schema(description = "状态：settled")
    @TableField("status")
    private String status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

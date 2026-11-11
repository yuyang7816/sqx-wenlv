package com.sqx.app.lvyou.coupon.domain.entity;

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
 * 用户优惠券
 */
@Getter
@Setter
@TableName("tb_user_coupon")
@Schema(name = "UserCouponEntity", description = "用户优惠券")
public class UserCouponEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户券业务编码")
    @TableField("coupon_code")
    private String couponCode;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "模板ID")
    @TableField("template_id")
    private Long templateId;

    @Schema(description = "模板业务编码")
    @TableField("template_code")
    private String templateCode;

    @Schema(description = "优惠券名称")
    @TableField("name")
    private String name;

    @Schema(description = "优惠金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "最低消费门槛")
    @TableField("min_amount")
    private BigDecimal minAmount;

    @Schema(description = "适用范围")
    @TableField("scope")
    private String scope;

    @Schema(description = "状态：1未使用 2已使用 3已过期")
    @TableField("status")
    private Integer status;

    @Schema(description = "生效时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("valid_start")
    private LocalDateTime validStart;

    @Schema(description = "失效时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("valid_end")
    private LocalDateTime validEnd;

    @Schema(description = "使用的订单号")
    @TableField("used_order_no")
    private String usedOrderNo;

    @Schema(description = "使用时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("used_time")
    private LocalDateTime usedTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

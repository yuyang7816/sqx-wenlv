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
 * 优惠券模板
 */
@Getter
@Setter
@TableName("tb_coupon_template")
@Schema(name = "CouponTemplateEntity", description = "优惠券模板")
public class CouponTemplateEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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

    @Schema(description = "适用范围：all/hotel/route/goods")
    @TableField("scope")
    private String scope;

    @Schema(description = "领取后有效天数")
    @TableField("valid_days")
    private Integer validDays;

    @Schema(description = "使用说明")
    @TableField("description")
    private String description;

    @Schema(description = "发放总量，0不限")
    @TableField("total_count")
    private Integer totalCount;

    @Schema(description = "已领取数量")
    @TableField("claimed_count")
    private Integer claimedCount;

    @Schema(description = "每人限领张数")
    @TableField("per_user_limit")
    private Integer perUserLimit;

    @Schema(description = "状态：1可领取 0下架")
    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

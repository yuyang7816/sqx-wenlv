package com.sqx.app.lvyou.coupon.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端优惠券模板
 */
@Data
@Schema(description = "管理端优惠券模板")
public class CouponTemplateVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "模板业务编码")
    private String templateCode;

    @Schema(description = "优惠券名称")
    private String name;

    @Schema(description = "优惠金额")
    private BigDecimal amount;

    @Schema(description = "最低消费门槛")
    private BigDecimal minAmount;

    @Schema(description = "适用范围：all/hotel/route/goods")
    private String scope;

    @Schema(description = "适用范围文案")
    private String scopeLabel;

    @Schema(description = "领取后有效天数")
    private Integer validDays;

    @Schema(description = "使用说明")
    private String description;

    @Schema(description = "发放总量，0不限")
    private Integer totalCount;

    @Schema(description = "已领取数量")
    private Integer claimedCount;

    @Schema(description = "每人限领张数")
    private Integer perUserLimit;

    @Schema(description = "状态：1可领取 0下架")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

package com.sqx.app.lvyou.order.domain.entity;

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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Getter
@Setter
@TableName("tb_order")
@Schema(name = "OrderEntity", description = "订单")
public class OrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单号")
    @TableField("order_no")
    private String orderNo;

    @Schema(description = "下单用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "商户ID")
    @TableField("merchant_id")
    private Long merchantId;

    @Schema(description = "目的地ID")
    @TableField("destination_id")
    private Long destinationId;

    @Schema(description = "订单分类：hotel/route/goods")
    @TableField("order_category")
    private String orderCategory;

    @Schema(description = "订单类型（权威）：hotel/route/goods/culture/service/restaurant")
    @TableField("order_type")
    private String orderType;

    @Schema(description = "订单状态")
    @TableField("status")
    private Integer status;

    @Schema(description = "状态展示文案")
    @TableField("status_text")
    private String statusText;

    @Schema(description = "订单总额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "实付金额")
    @TableField("pay_amount")
    private BigDecimal payAmount;

    @Schema(description = "用户退款金额")
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    @Schema(description = "商户结算入账金额")
    @TableField("merchant_settle_amount")
    private BigDecimal merchantSettleAmount;

    @Schema(description = "平台抽成金额")
    @TableField("platform_fee_amount")
    private BigDecimal platformFeeAmount;

    @Schema(description = "商户结算完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("settle_time")
    private LocalDateTime settleTime;

    @Schema(description = "优惠抵扣金额")
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    @Schema(description = "使用的用户优惠券ID")
    @TableField("coupon_id")
    private Long couponId;

    @Schema(description = "支付方式")
    @TableField("pay_method")
    private String payMethod;

    @Schema(description = "支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("pay_time")
    private LocalDateTime payTime;

    @Schema(description = "支付截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("pay_expire_time")
    private LocalDateTime payExpireTime;

    @Schema(description = "核销/入住确认时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("verify_time")
    private LocalDateTime verifyTime;

    @Schema(description = "核销码（酒店等）")
    @TableField("verify_code")
    private String verifyCode;

    @Schema(description = "联系人姓名")
    @TableField("contact_name")
    private String contactName;

    @Schema(description = "联系人手机号")
    @TableField("contact_phone")
    private String contactPhone;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "价格快照JSON")
    @TableField("price_snapshot_json")
    private String priceSnapshotJson;

    @Schema(description = "退改规则快照JSON")
    @TableField("cancel_policy_json")
    private String cancelPolicyJson;

    @Schema(description = "入住日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("check_in_date")
    private LocalDate checkInDate;

    @Schema(description = "离店日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("check_out_date")
    private LocalDate checkOutDate;

    @Schema(description = "入住晚数")
    @TableField("nights")
    private Integer nights;

    @Schema(description = "预订间数")
    @TableField("room_count")
    private Integer roomCount;

    @Schema(description = "入住人/出行人姓名")
    @TableField("guest_names")
    private String guestNames;

    @Schema(description = "出行人明细JSON")
    @TableField("guest_info_json")
    private String guestInfoJson;

    @Schema(description = "出行日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("travel_date")
    private LocalDate travelDate;

    @Schema(description = "出行人数")
    @TableField("guest_count")
    private Integer guestCount;

    @Schema(description = "预计到店时间")
    @TableField("expected_arrival_time")
    private String expectedArrivalTime;

    @Schema(description = "集合地点")
    @TableField("meet_point")
    private String meetPoint;

    @Schema(description = "收货人")
    @TableField("receiver_name")
    private String receiverName;

    @Schema(description = "收货电话")
    @TableField("receiver_phone")
    private String receiverPhone;

    @Schema(description = "收货地址")
    @TableField("receiver_address")
    private String receiverAddress;

    @Schema(description = "商户名称")
    @TableField("merchant_name")
    private String merchantName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

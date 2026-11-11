package com.sqx.app.lvyou.order.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端订单 VO（列表/详情）
 */
@Data
@Schema(description = "用户端订单")
public class OrderAppVo {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "订单分类：hotel/route/goods")
    private String orderCategory;

    @Schema(description = "订单类型（权威）：hotel/route/goods/culture/service/restaurant")
    private String orderType;

    @Schema(description = "订单类型展示文案")
    private String orderTypeLabel;

    @Schema(description = "C 端 Tab 分类：hotel/route/goods")
    private String userTabType;

    @Schema(description = "状态码（API 用）")
    private String statusCode;

    @Schema(description = "状态展示文案")
    private String status;

    @Schema(description = "状态颜色")
    private String statusColor;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "商户联系电话")
    private String merchantPhone;

    @Schema(description = "商户联系人")
    private String merchantContactName;

    @Schema(description = "商户门店地址")
    private String merchantAddress;

    @Schema(description = "商户所在地区")
    private String merchantRegion;

    @Schema(description = "目的地名称")
    private String destinationName;

    @Schema(description = "商户纬度")
    private BigDecimal latitude;

    @Schema(description = "商户经度")
    private BigDecimal longitude;

    @Schema(description = "酒店ID（酒店订单跳转详情）")
    private Long hotelId;

    @Schema(description = "酒店封面（酒店订单商家展示）")
    private String hotelCover;

    @Schema(description = "商户门店封面（线路等订单商家展示）")
    private String merchantCover;

    @Schema(description = "房型ID（跳转房型详情）")
    private Long roomId;

    @Schema(description = "房型规格")
    private String roomSpec;

    @Schema(description = "实付金额")
    private BigDecimal amount;

    @Schema(description = "订单总额")
    private BigDecimal totalAmount;

    @Schema(description = "优惠抵扣")
    private BigDecimal discountAmount;

    @Schema(description = "运费（好物订单）")
    private BigDecimal shippingFee;

    @Schema(description = "下单时间")
    private String date;

    @Schema(description = "支付方式")
    private String payMethod;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "房型/商品名称")
    private String roomName;

    @Schema(description = "线路名称")
    private String routeName;

    @Schema(description = "线路ID（跳转门店/线路详情）")
    private Long routeId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品封面")
    private String goodsCover;

    @Schema(description = "商品规格")
    private String goodsSpec;

    @Schema(description = "购买数量")
    private Integer quantity;

    @Schema(description = "入住日期")
    private String checkIn;

    @Schema(description = "离店日期")
    private String checkOut;

    @Schema(description = "入住晚数")
    private Integer nights;

    @Schema(description = "预订间数")
    private Integer roomCount;

    @Schema(description = "预计到店时间")
    private String expectedArrivalTime;

    @Schema(description = "支付截止时间")
    private String payExpireTime;

    @Schema(description = "退改规则摘要")
    private String cancelPolicySummary;

    @Schema(description = "是否可免费取消")
    private Boolean canCancelOrder;

    @Schema(description = "入住人/出行人")
    private String guestNames;

    @Schema(description = "出行日期")
    private String tripDate;

    @Schema(description = "出行人数")
    private Integer guestCount;

    @Schema(description = "行程时长")
    private String duration;

    @Schema(description = "途经景点")
    private String spots;

    @Schema(description = "集合地点")
    private String meetPoint;

    @Schema(description = "收货地址全文")
    private String addressText;

    @Schema(description = "订单明细")
    private List<OrderAppItemVo> items;

    @Schema(description = "主商品ID（商品类订单，用于跳转评价）")
    private Long goodsId;

    @Schema(description = "是否可确认收货")
    private Boolean canConfirmReceive;

    @Schema(description = "是否可申请退款")
    private Boolean canApplyRefund;

    @Schema(description = "是否可评价（商品类已完成且未评价）")
    private Boolean canReview;

    @Schema(description = "是否已评价")
    private Boolean reviewed;

    @Schema(description = "核销时间（酒店等）")
    private String verifiedAt;

    @Schema(description = "是否已核销")
    private Boolean verified;

    @Schema(description = "核销码（待入住时展示）")
    private String verifyCode;

    @Schema(description = "线路出行人列表")
    private List<OrderGuestVo> guestList;

    @Schema(description = "线路退款预览")
    private OrderRefundPreviewVo refundPreview;

    @Schema(description = "用户退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "商户结算入账金额")
    private BigDecimal merchantSettleAmount;

    @Schema(description = "平台抽成金额")
    private BigDecimal platformFeeAmount;

    @Schema(description = "结算完成时间")
    private String settleTime;
}

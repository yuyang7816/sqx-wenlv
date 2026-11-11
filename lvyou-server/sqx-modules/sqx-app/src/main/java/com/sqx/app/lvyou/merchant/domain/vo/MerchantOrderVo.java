package com.sqx.app.lvyou.merchant.domain.vo;

import com.sqx.app.lvyou.order.domain.vo.OrderGuestVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商户订单 VO（列表/详情）
 */
@Data
@Schema(description = "商户订单")
public class MerchantOrderVo {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "订单状态码（前端 Tab 筛选用）")
    private String status;

    @Schema(description = "订单状态文案")
    private String statusLabel;

    @Schema(description = "下单用户姓名")
    private String customerName;

    @Schema(description = "联系电话")
    private String customerPhone;

    @Schema(description = "订单金额（实付）")
    private BigDecimal amount;

    @Schema(description = "订单总额")
    private BigDecimal totalAmount;

    @Schema(description = "优惠抵扣")
    private BigDecimal discountAmount;

    @Schema(description = "运费（好物订单）")
    private BigDecimal shippingFee;

    @Schema(description = "预订间数（酒店）")
    private Integer roomCount;

    @Schema(description = "入住人/出行人姓名")
    private String guestNames;

    @Schema(description = "预计到店时间（酒店）")
    private String expectedArrivalTime;

    @Schema(description = "集合地点（线路）")
    private String meetPoint;

    @Schema(description = "收货人（商品）")
    private String receiverName;

    @Schema(description = "收货电话（商品）")
    private String receiverPhone;

    @Schema(description = "下单时间")
    private String createdAt;

    @Schema(description = "支付时间")
    private String paidAt;

    @Schema(description = "用户备注")
    private String remark;

    @Schema(description = "订单明细")
    private List<MerchantOrderItemVo> items;

    @Schema(description = "列表标题摘要")
    private String title;

    @Schema(description = "列表客户摘要")
    private String customer;

    @Schema(description = "列表时间摘要")
    private String time;

    @Schema(description = "入住日期（酒店）")
    private String checkIn;

    @Schema(description = "离店日期（酒店）")
    private String checkOut;

    @Schema(description = "入住晚数（酒店）")
    private Integer nights;

    @Schema(description = "预约日期（餐饮）")
    private String reservationDate;

    @Schema(description = "预约时间（餐饮）")
    private String reservationTime;

    @Schema(description = "人数")
    private Integer guestCount;

    @Schema(description = "出团日期（线路）")
    private String tourDate;

    @Schema(description = "收货地址（商品）")
    private String address;

    @Schema(description = "订单分类：hotel/route/goods/restaurant")
    private String orderCategory;

    @Schema(description = "订单类型（权威）")
    private String orderType;

    @Schema(description = "订单类型展示文案")
    private String orderTypeLabel;

    @Schema(description = "核销时间")
    private String verifiedAt;

    @Schema(description = "是否已核销")
    private Boolean verified;

    @Schema(description = "核销码")
    private String verifyCode;

    @Schema(description = "线路出行人列表（全量）")
    private List<OrderGuestVo> guestList;
}

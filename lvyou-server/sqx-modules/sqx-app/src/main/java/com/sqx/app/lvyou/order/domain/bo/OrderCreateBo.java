package com.sqx.app.lvyou.order.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建酒店/线路订单
 */
@Data
@Schema(description = "创建酒店/线路订单")
public class OrderCreateBo {

    @NotBlank(message = "订单分类不能为空")
    @Schema(description = "订单分类：hotel/route")
    private String category;

    @Schema(description = "酒店ID（category=hotel 时必填）")
    private Long hotelId;

    @Schema(description = "房型ID（category=hotel 时必填）")
    private Long roomId;

    @Schema(description = "线路ID（category=route 时必填）")
    private Long routeId;

    @Schema(description = "入住日期 yyyy-MM-dd（酒店）")
    private String checkInDate;

    @Schema(description = "离店日期 yyyy-MM-dd（酒店）")
    private String checkOutDate;

    @Schema(description = "出行日期 yyyy-MM-dd（线路）")
    private String travelDate;

    @Min(value = 1, message = "出行人数至少为1")
    @Schema(description = "出行/入住人数")
    private Integer guestCount;

    @Min(value = 1, message = "预订间数至少为1")
    @Schema(description = "预订间数（酒店）")
    private Integer roomCount;

    @Schema(description = "预计到店时间（酒店，如 18:00 前）")
    private String expectedArrivalTime;

    @Schema(description = "入住人/出行人姓名列表")
    private List<String> guestNames;

    @Schema(description = "线路出行人明细")
    private List<OrderGuestBo> guestList;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人手机号")
    private String contactPhone;

    @Schema(description = "用户优惠券ID")
    private Long couponId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "客户端传入单价（校验用，可选）")
    private BigDecimal unitPrice;
}

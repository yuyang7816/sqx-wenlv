package com.sqx.app.lvyou.order.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 物流查询结果
 */
@Data
@Schema(description = "物流查询结果")
public class OrderLogisticsVo {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "订单状态码")
    private Integer orderStatus;

    @Schema(description = "状态文案")
    private String statusText;

    @Schema(description = "物流公司")
    private String logisticsCompany;

    @Schema(description = "物流单号")
    private String logisticsNo;

    @Schema(description = "收货人")
    private String receiverName;

    @Schema(description = "收货电话")
    private String receiverPhone;

    @Schema(description = "完整收货地址")
    private String fullAddress;

    @Schema(description = "当前物流状态描述")
    private String currentStatus;

    @Schema(description = "是否已发货")
    private Boolean shipped;

    @Schema(description = "是否可查询第三方物流")
    private Boolean thirdPartyQuery;

    @Schema(description = "物流查询提示")
    private String queryTip;

    @Schema(description = "物流轨迹")
    private List<OrderLogisticsTrackVo> tracks;
}

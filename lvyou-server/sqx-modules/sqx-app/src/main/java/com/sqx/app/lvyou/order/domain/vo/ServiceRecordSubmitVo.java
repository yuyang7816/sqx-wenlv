package com.sqx.app.lvyou.order.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 服务预约提交结果
 */
@Data
@Schema(description = "服务预约提交结果")
public class ServiceRecordSubmitVo {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "订单状态码")
    private String status;

    @Schema(description = "订单状态文案")
    private String statusLabel;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "预约意向")
    private String intent;
}

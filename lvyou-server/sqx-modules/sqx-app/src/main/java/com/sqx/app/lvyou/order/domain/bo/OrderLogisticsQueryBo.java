package com.sqx.app.lvyou.order.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 物流查询请求
 */
@Data
public class OrderLogisticsQueryBo {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID")
    private Long id;
}

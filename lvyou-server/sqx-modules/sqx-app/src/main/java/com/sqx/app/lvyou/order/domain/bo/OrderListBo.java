package com.sqx.app.lvyou.order.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端订单列表查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户端订单列表查询")
public class OrderListBo extends PageParam {

    @Schema(description = "订单分类：hotel/route/goods")
    private String category;

    @Schema(description = "状态筛选：pending_pay/pending_use/completed/cancelled 等")
    private String status;

    @Schema(description = "订单号模糊筛选")
    private String orderNo;
}

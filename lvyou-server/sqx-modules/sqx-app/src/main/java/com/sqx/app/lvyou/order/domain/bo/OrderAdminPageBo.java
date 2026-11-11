package com.sqx.app.lvyou.order.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理端订单分页查询
 */
@Getter
@Setter
@Schema(description = "管理端订单分页查询")
public class OrderAdminPageBo extends PageParam {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号模糊筛选")
    private String orderNo;

    @Schema(description = "订单分类：hotel/route/goods/service")
    private String category;

    @Schema(description = "订单状态：pending_pay/pending_confirm/completed 等")
    private String status;

    @Schema(description = "联系人手机号模糊筛选")
    private String contactPhone;

    @Schema(description = "商户名称模糊筛选")
    private String merchantName;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "目的地ID")
    private Long destinationId;
}

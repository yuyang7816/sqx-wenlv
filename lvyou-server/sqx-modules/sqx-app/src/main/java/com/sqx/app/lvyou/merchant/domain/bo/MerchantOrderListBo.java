package com.sqx.app.lvyou.merchant.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商户订单列表查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "商户订单列表查询")
public class MerchantOrderListBo extends PageParam {

    @Schema(description = "Tab：all/pending/processing/completed/verify")
    private String tab;

    @Schema(description = "订单号模糊筛选")
    private String orderNo;

    @Schema(description = "订单状态码筛选（前端 status 字符串）")
    private String status;
}

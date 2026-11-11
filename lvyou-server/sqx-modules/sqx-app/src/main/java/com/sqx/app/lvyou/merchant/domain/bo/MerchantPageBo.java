package com.sqx.app.lvyou.merchant.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端商户分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "商户分页查询")
public class MerchantPageBo extends PageParam {

    @Schema(description = "商户ID")
    private Long id;

    @Schema(description = "商户业务编码")
    private String merchantCode;

    @Schema(description = "门店名称（模糊）")
    private String storeName;

    @Schema(description = "联系电话（模糊）")
    private String contactPhone;

    @Schema(description = "门店类型：hotel/restaurant/travel/culture/goose")
    private String storeType;

    @Schema(description = "账号状态：1正常 2禁用")
    private Integer status;

    @Schema(description = "营业状态：1营业中 0休息中")
    private Integer isOpen;

    @Schema(description = "所属目的地ID")
    private Long destinationId;
}

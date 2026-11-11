package com.sqx.app.lvyou.merchant.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端商户入驻申请分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "商户入驻申请分页查询")
public class MerchantApplyPageBo extends PageParam {

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "申请单号")
    private String applyNo;

    @Schema(description = "门店名称（模糊）")
    private String storeName;

    @Schema(description = "联系人（模糊）")
    private String contactName;

    @Schema(description = "联系电话（模糊）")
    private String contactPhone;

    @Schema(description = "门店类型：hotel/restaurant/travel/culture")
    private String storeType;

    @Schema(description = "审核状态：0待审核 1通过 2驳回")
    private Integer auditStatus;

    @Schema(description = "意向目的地ID")
    private Long destinationId;
}

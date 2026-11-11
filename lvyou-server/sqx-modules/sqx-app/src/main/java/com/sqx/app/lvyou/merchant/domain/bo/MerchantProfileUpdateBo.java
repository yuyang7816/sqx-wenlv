package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 商户资料更新
 */
@Getter
@Setter
@Schema(description = "商户资料更新")
public class MerchantProfileUpdateBo {

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "门店地址")
    private String storeAddress;

    @Schema(description = "营业时间")
    private String storeHours;

    @Schema(description = "是否营业中")
    private Boolean isOpen;
}

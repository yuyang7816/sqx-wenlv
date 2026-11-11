package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 商户服务上下架
 */
@Getter
@Setter
@Schema(description = "商户服务上下架")
public class MerchantServicePublishBo {

    @NotBlank(message = "请指定目标状态")
    @Schema(description = "目标状态：on_sale/off_sale")
    private String status;
}

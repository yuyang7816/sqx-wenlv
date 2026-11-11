package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 管理端更新商户
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新商户")
public class MerchantUpdateBo extends MerchantSaveBo {

    @Schema(description = "商户ID")
    @NotNull(message = "商户ID不能为空")
    private Long id;
}

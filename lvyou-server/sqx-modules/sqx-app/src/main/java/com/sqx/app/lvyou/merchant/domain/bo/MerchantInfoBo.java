package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理端商户主键操作
 */
@Data
@Schema(description = "商户主键")
public class MerchantInfoBo {

    @Schema(description = "商户ID")
    @NotNull(message = "商户ID不能为空")
    private Long id;
}

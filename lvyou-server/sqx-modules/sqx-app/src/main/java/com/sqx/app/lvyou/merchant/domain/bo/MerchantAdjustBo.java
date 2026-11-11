package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理端商户状态调整
 */
@Data
@Schema(description = "商户状态调整")
public class MerchantAdjustBo {

    @Schema(description = "商户ID")
    @NotNull(message = "商户ID不能为空")
    private Long id;

    @Schema(description = "调整动作：enable 启用 / disable 禁用 / open 营业 / close 休息 / homeRecommendOn 开启首页推荐 / homeRecommendOff 关闭首页推荐")
    @NotBlank(message = "调整动作不能为空")
    private String action;

    @Schema(description = "禁用原因（禁用时可选填）")
    private String reason;
}

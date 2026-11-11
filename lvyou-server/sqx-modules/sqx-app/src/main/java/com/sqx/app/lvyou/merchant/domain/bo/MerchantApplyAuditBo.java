package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理端审核商户入驻申请
 */
@Data
@Schema(description = "商户入驻审核")
public class MerchantApplyAuditBo {

    @Schema(description = "申请ID")
    @NotNull(message = "申请ID不能为空")
    private Long id;

    @Schema(description = "审核动作：pass 通过 / reject 驳回")
    @NotBlank(message = "审核动作不能为空")
    private String action;

    @Schema(description = "驳回原因（驳回时必填）")
    private String reason;
}

package com.sqx.app.lvyou.wallet.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理端提现审核
 */
@Data
@Schema(description = "提现审核")
public class WithdrawAdminAuditBo {

    @NotNull(message = "记录ID不能为空")
    @Schema(description = "提现记录ID")
    private Long id;

    @NotBlank(message = "审核动作不能为空")
    @Schema(description = "审核动作：pass 通过 / reject 驳回")
    private String action;

    @Schema(description = "驳回原因（驳回时必填）")
    private String reason;
}

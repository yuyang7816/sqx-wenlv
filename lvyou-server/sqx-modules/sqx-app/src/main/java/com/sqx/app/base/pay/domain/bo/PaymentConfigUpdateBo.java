package com.sqx.app.base.pay.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理端支付渠道配置更新
 */
@Data
@Schema(description = "管理端支付渠道配置更新")
public class PaymentConfigUpdateBo {

    @NotNull(message = "配置ID不能为空")
    @Schema(description = "sys_config.id")
    private Long id;

    @NotBlank(message = "配置键不能为空")
    @Schema(description = "配置键")
    private String paramKey;

    @Schema(description = "配置值")
    private String paramValue;
}

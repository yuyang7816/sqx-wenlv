package com.sqx.app.base.pay.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端支付渠道配置 VO
 */
@Data
@Schema(description = "管理端支付渠道配置")
public class PaymentConfigAdminVo {

    @Schema(description = "sys_config.id")
    private Long id;

    @Schema(description = "配置键")
    private String paramKey;

    @Schema(description = "配置值")
    private String paramValue;

    @Schema(description = "配置说明")
    private String description;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否已配置")
    private Boolean configured;
}

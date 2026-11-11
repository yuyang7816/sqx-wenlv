package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 管理端新增商户（字段与 C 端入驻申请一致，并补充目的地/坐标/绑定用户）
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "新增商户")
public class MerchantSaveBo extends MerchantApplyBo {

    @Schema(description = "绑定用户ID")
    @NotNull(message = "请选择关联用户")
    private Long userId;

    @Schema(description = "所属目的地ID")
    @NotNull(message = "所属目的地不能为空")
    private Long destinationId;

    @Schema(description = "账号状态：1正常 2禁用")
    private Integer status;

    @Schema(description = "营业状态：1营业中 0休息中")
    private Integer isOpen;
}

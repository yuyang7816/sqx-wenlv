package com.sqx.app.lvyou.coupon.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户端领券
 */
@Data
@Schema(description = "领券请求")
public class CouponClaimBo {

    @Schema(description = "模板业务编码（与前端 templateId 对齐）")
    @NotBlank(message = "模板编码不能为空")
    private String templateId;
}

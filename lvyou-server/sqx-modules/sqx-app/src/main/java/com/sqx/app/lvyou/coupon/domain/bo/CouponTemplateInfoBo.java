package com.sqx.app.lvyou.coupon.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 优惠券模板主键操作
 */
@Data
@Schema(description = "优惠券模板主键")
public class CouponTemplateInfoBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;
}

package com.sqx.app.lvyou.heritage.shop.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理端更新非遗店铺状态
 */
@Data
@Schema(description = "更新非遗店铺状态")
public class HeritageShopUpdateStateBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "状态：1营业 0歇业")
    @NotNull(message = "状态不能为空")
    private Integer status;
}

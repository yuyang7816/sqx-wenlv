package com.sqx.app.lvyou.heritage.shop.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 非遗好店分类启用/禁用
 */
@Data
@Schema(description = "非遗好店分类启用/禁用")
public class HeritageShopCategoryUpdateStateBo {

    @NotNull(message = "id不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    @Schema(description = "1启用 0禁用")
    private Integer status;
}

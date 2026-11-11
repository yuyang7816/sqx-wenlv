package com.sqx.app.lvyou.heritage.shop.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 更新非遗好店分类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新非遗好店分类")
public class HeritageShopCategoryUpdateBo extends HeritageShopCategorySaveBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;
}

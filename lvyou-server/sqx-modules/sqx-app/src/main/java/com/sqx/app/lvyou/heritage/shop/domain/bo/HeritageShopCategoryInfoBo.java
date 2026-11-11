package com.sqx.app.lvyou.heritage.shop.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 非遗好店分类主键
 */
@Data
@Schema(description = "非遗好店分类主键")
public class HeritageShopCategoryInfoBo {

    @NotNull(message = "id不能为空")
    private Long id;
}

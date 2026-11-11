package com.sqx.app.lvyou.heritage.goods.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 非遗商品ID入参
 */
@Data
@Schema(description = "非遗商品ID")
public class GoodsInfoBo {

    @Schema(description = "商品ID")
    @NotNull(message = "商品ID不能为空")
    private Long id;
}

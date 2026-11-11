package com.sqx.app.lvyou.heritage.goods.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 管理端更新非遗商品
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新非遗商品")
public class GoodsUpdateBo extends GoodsSaveBo {

    @Schema(description = "商品ID")
    @NotNull(message = "商品ID不能为空")
    private Long id;
}

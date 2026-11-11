package com.sqx.app.lvyou.heritage.goods.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理端更新非遗好物上下架状态
 */
@Data
@Schema(description = "更新非遗好物状态")
public class GoodsUpdateStateBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "状态：1上架 0下架")
    @NotNull(message = "状态不能为空")
    private Integer status;
}

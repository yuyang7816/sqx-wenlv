package com.sqx.app.lvyou.heritage.shop.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 非遗店铺主键操作
 */
@Data
@Schema(description = "非遗店铺主键")
public class HeritageShopInfoBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;
}

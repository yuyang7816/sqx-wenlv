package com.sqx.app.lvyou.heritage.craft.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 非遗工艺主键操作
 */
@Data
@Schema(description = "非遗工艺主键")
public class HeritageCraftInfoBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;
}

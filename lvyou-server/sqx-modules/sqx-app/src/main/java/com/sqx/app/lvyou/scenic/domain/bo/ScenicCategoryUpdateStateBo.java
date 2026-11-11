package com.sqx.app.lvyou.scenic.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 景区分类启用/禁用
 */
@Data
@Schema(description = "景区分类启用/禁用")
public class ScenicCategoryUpdateStateBo {

    @NotNull(message = "id不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    @Schema(description = "1启用 0禁用")
    private Integer status;
}

package com.sqx.app.lvyou.scenic.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 更新景区分类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新景区分类")
public class ScenicCategoryUpdateBo extends ScenicCategorySaveBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;
}

package com.sqx.app.lvyou.scenic.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 新增景区分类
 */
@Data
@Schema(description = "新增景区分类")
public class ScenicCategorySaveBo {

    @Schema(description = "分类编码，仅小写字母数字下划线")
    @NotBlank(message = "分类编码不能为空")
    private String code;

    @Schema(description = "分类名称")
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "排序")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态：1启用 0禁用")
    @NotNull(message = "状态不能为空")
    private Integer status;
}

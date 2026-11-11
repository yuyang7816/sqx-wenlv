package com.sqx.app.lvyou.scenic.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端/下拉景区分类选项
 */
@Data
@Schema(description = "景区分类选项")
public class ScenicCategoryOptionVo {

    @Schema(description = "分类编码")
    private String code;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序")
    private Integer sort;
}

package com.sqx.app.lvyou.heritage.shop.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端/下拉非遗好店分类选项
 */
@Data
@Schema(description = "非遗好店分类选项")
public class HeritageShopCategoryOptionVo {

    @Schema(description = "分类编码")
    private String code;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序")
    private Integer sort;
}

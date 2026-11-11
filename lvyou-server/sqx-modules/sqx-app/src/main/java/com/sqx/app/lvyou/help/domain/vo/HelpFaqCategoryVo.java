package com.sqx.app.lvyou.help.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端 FAQ 分类分组
 */
@Data
@Schema(description = "FAQ分类")
public class HelpFaqCategoryVo {

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "该分类下的问答列表")
    private List<HelpFaqItemVo> items;
}

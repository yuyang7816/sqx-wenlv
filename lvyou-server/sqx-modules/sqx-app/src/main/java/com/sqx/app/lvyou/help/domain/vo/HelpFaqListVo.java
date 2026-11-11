package com.sqx.app.lvyou.help.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端 FAQ 分类列表
 */
@Data
@Schema(description = "FAQ分类问答列表")
public class HelpFaqListVo {

    @Schema(description = "按分类分组的问答")
    private List<HelpFaqCategoryVo> categories;
}

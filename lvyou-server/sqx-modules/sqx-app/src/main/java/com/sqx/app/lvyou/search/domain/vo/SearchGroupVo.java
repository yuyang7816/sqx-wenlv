package com.sqx.app.lvyou.search.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 综合搜索分组
 */
@Data
@Schema(name = "SearchGroupVo", description = "综合搜索分组")
public class SearchGroupVo {

    @Schema(description = "内容类型：hotel/route/scenic/activity/goods/shop")
    private String type;

    @Schema(description = "分组中文名")
    private String label;

    @Schema(description = "匹配条目，每类最多5条")
    private List<SearchItemVo> items = new ArrayList<>();
}

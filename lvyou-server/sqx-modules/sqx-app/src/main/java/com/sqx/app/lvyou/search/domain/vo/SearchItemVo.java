package com.sqx.app.lvyou.search.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 综合搜索条目
 */
@Data
@Schema(name = "SearchItemVo", description = "综合搜索条目")
public class SearchItemVo {

    @Schema(description = "资源ID")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "封面图URL")
    private String cover;

    @Schema(description = "起步价/售价")
    private BigDecimal price;

    @Schema(description = "展示标签")
    private String tag;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "评分")
    private BigDecimal score;
}

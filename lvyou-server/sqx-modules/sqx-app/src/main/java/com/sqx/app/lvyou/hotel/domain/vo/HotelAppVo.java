package com.sqx.app.lvyou.hotel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端酒店列表项
 */
@Data
@Schema(description = "用户端酒店列表项")
public class HotelAppVo {

    @Schema(description = "酒店ID")
    private Long id;

    @Schema(description = "酒店名称")
    private String name;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "分类文案")
    private String categoryLabel;

    @Schema(description = "星级")
    private Integer starLevel;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "评分")
    private BigDecimal score;

    @Schema(description = "标签")
    private String tag;

    @Schema(description = "起步价")
    private BigDecimal priceFrom;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "与用户距离（公里）")
    private BigDecimal distance;
}

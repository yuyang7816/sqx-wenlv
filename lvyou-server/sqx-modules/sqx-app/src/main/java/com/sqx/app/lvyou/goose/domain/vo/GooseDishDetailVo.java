package com.sqx.app.lvyou.goose.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 灰鹅美食品详情
 */
@Getter
@Setter
@Schema(description = "灰鹅美食品详情")
public class GooseDishDetailVo {

    @Schema(description = "菜品ID")
    private Long id;

    @Schema(description = "菜品名称")
    private String name;

    @Schema(description = "菜品说明")
    private String desc;

    @Schema(description = "价格（元，字符串便于前端展示）")
    private String price;

    @Schema(description = "月售数量")
    private Integer sold;

    @Schema(description = "封面图")
    private String cover;

    @Schema(description = "轮播图")
    private List<String> banners;

    @Schema(description = "门店ID")
    private Long shopId;

    @Schema(description = "门店名称")
    private String shopName;

    @Schema(description = "门店封面")
    private String shopCover;

    @Schema(description = "门店地址")
    private String shopAddress;
}

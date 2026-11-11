package com.sqx.app.lvyou.heritage.goods.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端商品评价
 */
@Data
@Schema(description = "用户端商品评价")
public class ReviewAppVo {

    @Schema(description = "评价ID")
    private Long id;

    @Schema(description = "评价用户展示名")
    private String user;

    @Schema(description = "评价用户头像")
    private String avatar;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "评价时间（yyyy-MM-dd）")
    private String time;

    @Schema(description = "评分1-5")
    private Integer rating;

    @Schema(description = "评价图片URL列表")
    private List<String> images;
}

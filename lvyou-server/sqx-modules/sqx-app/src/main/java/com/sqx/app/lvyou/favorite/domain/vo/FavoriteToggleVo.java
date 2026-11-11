package com.sqx.app.lvyou.favorite.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 收藏切换结果
 */
@Data
@Schema(description = "收藏切换结果")
public class FavoriteToggleVo {

    @Schema(description = "切换后是否已收藏：true已收藏 false已取消")
    private Boolean favorited;
}

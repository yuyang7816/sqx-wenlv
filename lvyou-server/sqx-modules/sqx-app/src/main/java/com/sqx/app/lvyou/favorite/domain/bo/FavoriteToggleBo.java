package com.sqx.app.lvyou.favorite.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 收藏切换入参
 */
@Data
@Schema(description = "收藏切换入参")
public class FavoriteToggleBo {

    @Schema(description = "收藏对象类型：scenic/hotel/route/goods/goose/activity", required = true)
    @NotBlank(message = "收藏类型不能为空")
    private String targetType;

    @Schema(description = "收藏对象业务ID", required = true)
    @NotNull(message = "收藏对象ID不能为空")
    private Long targetId;
}

package com.sqx.app.lvyou.activity.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 活动亮点
 */
@Data
@Schema(description = "活动亮点")
public class ActivityHighlightVo {

    @Schema(description = "亮点标题")
    private String label;

    @Schema(description = "亮点描述")
    private String desc;
}

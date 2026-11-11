package com.sqx.app.lvyou.activity.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端活动列表查询
 */
@Data
@Schema(description = "用户端活动列表查询")
public class ActivityAppListBo {

    @Schema(description = "页码")
    private Long page;

    @Schema(description = "每页条数")
    private Long limit;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "活动名称（模糊）")
    private String name;

    @Schema(description = "活动阶段：all/ongoing/upcoming/ended")
    private String status;
}

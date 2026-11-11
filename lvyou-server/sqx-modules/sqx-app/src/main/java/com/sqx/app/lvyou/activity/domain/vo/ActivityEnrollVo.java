package com.sqx.app.lvyou.activity.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 活动报名结果
 */
@Data
@Schema(description = "活动报名结果")
public class ActivityEnrollVo {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "报名记录ID")
    private Long enrollmentId;

    @Schema(description = "已参与人数")
    private Integer participants;
}

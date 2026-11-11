package com.sqx.app.lvyou.goose.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 灰鹅美食营业时间
 */
@Getter
@Setter
@Schema(description = "灰鹅美食营业时间")
public class GooseBusinessHourVo {

    @Schema(description = "序号")
    private Long id;

    @Schema(description = "时段标签")
    private String label;

    @Schema(description = "营业时间")
    private String time;
}

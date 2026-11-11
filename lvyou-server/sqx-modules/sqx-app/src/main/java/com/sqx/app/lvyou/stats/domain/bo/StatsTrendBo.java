package com.sqx.app.lvyou.stats.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 趋势统计查询参数
 */
@Getter
@Setter
@Schema(name = "StatsTrendBo", description = "趋势统计查询参数")
public class StatsTrendBo {

    @Schema(description = "统计天数，默认 30，最大 90")
    @Min(1)
    @Max(90)
    private Integer days = 30;
}

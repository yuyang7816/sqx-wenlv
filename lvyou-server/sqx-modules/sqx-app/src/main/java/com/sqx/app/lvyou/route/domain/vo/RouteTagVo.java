package com.sqx.app.lvyou.route.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 线路标签
 */
@Data
@Schema(description = "线路标签")
public class RouteTagVo {

    @Schema(description = "标签文案")
    private String text;

    @Schema(description = "标签类型")
    private String type;
}

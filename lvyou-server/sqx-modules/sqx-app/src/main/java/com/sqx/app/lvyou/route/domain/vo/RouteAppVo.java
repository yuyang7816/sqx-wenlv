package com.sqx.app.lvyou.route.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端线路列表项
 */
@Data
@Schema(description = "用户端线路列表项")
public class RouteAppVo {

    @Schema(description = "线路ID")
    private Long id;

    @Schema(description = "线路名称")
    private String name;

    @Schema(description = "短描述")
    private String desc;

    @Schema(description = "起步价")
    private BigDecimal priceFrom;

    @Schema(description = "行程时长")
    private String duration;

    @Schema(description = "行程天数说明")
    private String days;

    @Schema(description = "标签列表")
    private List<RouteTagVo> tags;

    @Schema(description = "途经景点")
    private List<String> spots;

    @Schema(description = "封面图")
    private String cover;
}

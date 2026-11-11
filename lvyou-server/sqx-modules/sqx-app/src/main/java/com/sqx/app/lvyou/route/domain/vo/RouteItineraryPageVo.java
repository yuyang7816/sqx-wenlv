package com.sqx.app.lvyou.route.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端线路行程页
 */
@Data
@Schema(description = "用户端线路行程页")
public class RouteItineraryPageVo {

    @Schema(description = "线路ID")
    private Long id;

    @Schema(description = "线路名称")
    private String name;

    @Schema(description = "行程时长")
    private String duration;

    @Schema(description = "行程天数说明")
    private String days;

    @Schema(description = "行程天数")
    private Integer dayCount;

    @Schema(description = "旅行城市")
    private String travelCity;

    @Schema(description = "集合地点")
    private String meetPoint;

    @Schema(description = "出行日期规则说明")
    private String tripDateRule;

    @Schema(description = "行程节点列表")
    private List<RouteItineraryAppVo> itinerary;

    @Schema(description = "按天行程")
    private List<RouteItineraryDayAppVo> itineraryDays;
}

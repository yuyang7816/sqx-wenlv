package com.sqx.app.lvyou.route.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端行程节点
 */
@Data
@Schema(description = "用户端行程节点")
public class RouteItineraryAppVo {

    @Schema(description = "时间节点")
    private String time;

    @Schema(description = "节点名称")
    private String name;

    @Schema(description = "地点")
    private String location;

    @Schema(description = "节点描述")
    private String desc;

    @Schema(description = "封面图")
    private String cover;

    @Schema(description = "图片列表")
    private java.util.List<String> images;

    @Schema(description = "所属天序号")
    private Integer dayNo;

    @Schema(description = "天标签")
    private String dayLabel;
}

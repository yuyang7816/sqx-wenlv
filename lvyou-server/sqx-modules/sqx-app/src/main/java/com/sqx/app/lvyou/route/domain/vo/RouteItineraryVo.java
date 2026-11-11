package com.sqx.app.lvyou.route.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端行程节点
 */
@Data
@Schema(description = "管理端行程节点")
public class RouteItineraryVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "线路ID")
    private Long routeId;

    @Schema(description = "节点顺序")
    private Integer sort;

    @Schema(description = "时间节点")
    private String timeLabel;

    @Schema(description = "节点名称")
    private String name;

    @Schema(description = "地点")
    private String location;

    @Schema(description = "节点描述")
    private String description;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "节点图片列表")
    private java.util.List<String> images;
}

package com.sqx.app.lvyou.route.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 管理端行程节点
 */
@Data
@Schema(description = "行程节点")
public class RouteItinerarySaveBo {

    @Schema(description = "节点顺序")
    private Integer sort;

    @Schema(description = "时间节点")
    private String timeLabel;

    @Schema(description = "节点名称")
    @NotBlank(message = "节点名称不能为空")
    private String name;

    @Schema(description = "地点")
    private String location;

    @Schema(description = "节点描述")
    private String description;

    @Schema(description = "封面图（兼容首图）")
    private String coverImage;

    @Schema(description = "节点图片列表")
    private java.util.List<String> images;
}

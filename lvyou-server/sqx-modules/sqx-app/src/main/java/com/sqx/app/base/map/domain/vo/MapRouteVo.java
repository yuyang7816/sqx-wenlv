package com.sqx.app.base.map.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单条路线方案
 */
@Data
@Schema(description = "路线方案")
public class MapRouteVo {

    @Schema(description = "方案序号，从 0 开始")
    private Integer index;

    @Schema(description = "交通方式：DRIVING/WALKING/BICYCLING/TRANSIT")
    private String mode;

    @Schema(description = "标签，如少收费、不走高速")
    private List<String> tags = new ArrayList<>();

    @Schema(description = "方案摘要，公交如：地铁2号线 → 24路")
    private String summary;

    @Schema(description = "总距离（米）")
    private Integer distance;

    @Schema(description = "预计耗时（分钟）")
    private Integer duration;

    @Schema(description = "步行距离（米，公交方案）")
    private Integer walkDistance;

    @Schema(description = "换乘次数（公交方案）")
    private Integer transferCount;

    @Schema(description = "票价（分）")
    private Integer price;

    @Schema(description = "红绿灯数")
    private Integer trafficLightCount;

    @Schema(description = "过路费（元）")
    private Integer toll;

    @Schema(description = "距离文案")
    private String distanceText;

    @Schema(description = "耗时文案")
    private String durationText;

    @Schema(description = "步行距离文案")
    private String walkDistanceText;

    @Schema(description = "票价文案")
    private String priceText;

    @Schema(description = "路线坐标点")
    private List<MapLatLngVo> points = new ArrayList<>();

    @Schema(description = "导航步骤")
    private List<MapRouteStepVo> steps = new ArrayList<>();
}

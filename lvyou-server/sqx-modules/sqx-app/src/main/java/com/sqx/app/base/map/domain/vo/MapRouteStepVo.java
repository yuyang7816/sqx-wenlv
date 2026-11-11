package com.sqx.app.base.map.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "路线步骤")
public class MapRouteStepVo {

    @Schema(description = "本段交通方式：WALKING/TRANSIT/DRIVING 等")
    private String mode;

    @Schema(description = "指引文案")
    private String instruction;

    @Schema(description = "路名")
    private String roadName;

    @Schema(description = "方向")
    private String dirDesc;

    @Schema(description = "动作，如左转")
    private String actDesc;

    @Schema(description = "交通工具：BUS/SUBWAY/RAIL")
    private String vehicle;

    @Schema(description = "线路名称，如地铁2号线、24路")
    private String lineName;

    @Schema(description = "上车站")
    private String getOn;

    @Schema(description = "下车站")
    private String getOff;

    @Schema(description = "经停站数")
    private Integer stationCount;

    @Schema(description = "本段耗时（分钟）")
    private Integer duration;

    @Schema(description = "本段距离（米）")
    private Integer distance;

    @Schema(description = "距离文案")
    private String distanceText;
}

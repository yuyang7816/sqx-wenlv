package com.sqx.app.base.map.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "路线规划入参")
public class MapDirectionBo {

    @Schema(description = "起点纬度")
    private String fromLat;

    @Schema(description = "起点经度")
    private String fromLng;

    @Schema(description = "终点纬度")
    private String toLat;

    @Schema(description = "终点经度")
    private String toLng;

    @Schema(description = "交通方式：driving/walking/bicycling/transit，默认 driving")
    private String mode;

    @Schema(description = "是否返回多方案，默认 true")
    private Boolean multiPlan;
}

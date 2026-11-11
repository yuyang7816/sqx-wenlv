package com.sqx.app.lvyou.transport.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公交实时车辆信息
 */
@Data
@Schema(description = "公交实时车辆信息")
public class TransportTrackInfoVo {

    @Schema(description = "当前站")
    private String currentStation;

    @Schema(description = "下一站")
    private String nextStation;

    @Schema(description = "预计到站")
    private String eta;

    @Schema(description = "当前速度")
    private String speed;

    @Schema(description = "车牌号")
    private String vehicleNo;
}

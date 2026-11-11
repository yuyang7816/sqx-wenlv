package com.sqx.app.lvyou.transport.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端公交实时轨迹
 */
@Data
@Schema(description = "用户端公交实时轨迹")
public class TransportTrackAppVo {

    @Schema(description = "线路名称")
    private String routeName;

    @Schema(description = "实时车辆信息")
    private TransportTrackInfoVo trackInfo;

    @Schema(description = "实时动态")
    private List<TransportRealtimeUpdateVo> realtimeUpdates;

    @Schema(description = "线路轨迹点")
    private List<TransportPolylinePointVo> polyline;
}

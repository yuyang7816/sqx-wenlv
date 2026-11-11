package com.sqx.app.lvyou.transport.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端公交站点列表
 */
@Data
@Schema(description = "用户端公交站点列表")
public class TransportStationAppVo {

    @Schema(description = "线路业务编码")
    private String id;

    @Schema(description = "线路名称")
    private String name;

    @Schema(description = "起点站")
    private String startStation;

    @Schema(description = "终点站")
    private String endStation;

    @Schema(description = "站点数量")
    private Integer stationCount;

    @Schema(description = "当前站序号")
    private Integer currentStopIndex;

    @Schema(description = "站点名称列表")
    private List<String> stations;
}

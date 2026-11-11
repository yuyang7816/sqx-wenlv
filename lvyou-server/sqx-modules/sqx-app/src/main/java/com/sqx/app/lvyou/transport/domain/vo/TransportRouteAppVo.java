package com.sqx.app.lvyou.transport.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端公交线路列表项
 */
@Data
@Schema(description = "用户端公交线路列表项")
public class TransportRouteAppVo {

    @Schema(description = "线路业务编码")
    private String id;

    @Schema(description = "线路名称")
    private String name;

    @Schema(description = "首班时间")
    private String firstBus;

    @Schema(description = "末班时间")
    private String lastBus;

    @Schema(description = "起点站")
    private String startStation;

    @Schema(description = "终点站")
    private String endStation;

    @Schema(description = "运行状态键")
    private String statusKey;

    @Schema(description = "运行状态文案")
    private String statusLabel;

    @Schema(description = "主题色")
    private String accentColor;

    @Schema(description = "浅色主题")
    private String accentLight;

    @Schema(description = "是否展示实时站点")
    private Boolean showRealtimeStops;

    @Schema(description = "当前站")
    private String currentStop;

    @Schema(description = "下一站")
    private String nextStop;

    @Schema(description = "预计到站")
    private String nextEta;

    @Schema(description = "是否可展开站点")
    private Boolean expandable;

    @Schema(description = "站点数量")
    private Integer stationCount;

    @Schema(description = "当前站序号")
    private Integer currentStopIndex;

    @Schema(description = "站点名称列表")
    private List<String> stations;

    @Schema(description = "底部类型")
    private String footerType;

    @Schema(description = "实时车辆信息")
    private TransportTrackInfoVo trackInfo;

    @Schema(description = "apizero 官方城市名")
    private String city;

    @Schema(description = "查询参考站名")
    private String referenceStation;

    @Schema(description = "方向 1/2")
    private Integer direction;
}

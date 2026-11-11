package com.sqx.app.lvyou.transport.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端公交线路
 */
@Data
@Schema(description = "管理端公交线路")
public class TransportRouteVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "线路业务编码")
    private String routeCode;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "目的地名称")
    private String destinationName;

    @Schema(description = "纬度（发布选点）")
    private java.math.BigDecimal latitude;

    @Schema(description = "经度（发布选点）")
    private java.math.BigDecimal longitude;

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

    @Schema(description = "状态：1上架 0下架")
    private Integer status;

    @Schema(description = "运行状态键")
    private String statusKey;

    @Schema(description = "运行状态文案")
    private String statusLabel;

    @Schema(description = "主题色")
    private String accentColor;

    @Schema(description = "浅色主题")
    private String accentLight;

    @Schema(description = "是否展示实时站点")
    private Integer showRealtimeStops;

    @Schema(description = "是否可展开站点")
    private Integer expandable;

    @Schema(description = "底部类型")
    private String footerType;

    @Schema(description = "当前站序号")
    private Integer currentStopIndex;

    @Schema(description = "当前站名称")
    private String currentStop;

    @Schema(description = "下一站名称")
    private String nextStop;

    @Schema(description = "预计到站")
    private String nextEta;

    @Schema(description = "上下行方向")
    private List<TransportDirectionVo> directions;

    @Schema(description = "实时车辆信息")
    private TransportTrackInfoVo trackInfo;

    @Schema(description = "实时动态")
    private List<TransportRealtimeUpdateVo> realtimeUpdates;

    @Schema(description = "站点数量")
    private Integer stationCount;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

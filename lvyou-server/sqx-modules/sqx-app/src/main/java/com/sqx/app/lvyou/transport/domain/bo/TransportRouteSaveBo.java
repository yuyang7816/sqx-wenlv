package com.sqx.app.lvyou.transport.domain.bo;

import com.sqx.app.lvyou.transport.domain.vo.TransportDirectionVo;
import com.sqx.app.lvyou.transport.domain.vo.TransportRealtimeUpdateVo;
import com.sqx.app.lvyou.transport.domain.vo.TransportScheduleRowVo;
import com.sqx.app.lvyou.transport.domain.vo.TransportTrackInfoVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理端新增公交线路
 */
@Data
@Schema(description = "新增公交线路")
public class TransportRouteSaveBo {

    @Schema(description = "线路业务编码")
    @NotBlank(message = "线路编码不能为空")
    private String routeCode;

    @Schema(description = "目的地ID（服务端按经纬度自动解析）")
    private Long destinationId;

    @Schema(description = "发布位置纬度")
    private java.math.BigDecimal latitude;

    @Schema(description = "发布位置经度")
    private java.math.BigDecimal longitude;

    @Schema(description = "线路名称")
    @NotBlank(message = "线路名称不能为空")
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
    @NotNull(message = "状态不能为空")
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

    @Schema(description = "站点名称列表（按顺序）")
    private List<String> stations;

    @Schema(description = "时刻表行")
    private List<TransportScheduleRowVo> scheduleRows;

    @Schema(description = "排序")
    private Integer sort;
}

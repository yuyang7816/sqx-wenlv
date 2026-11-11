package com.sqx.app.lvyou.transport.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公交线路
 */
@Getter
@Setter
@TableName("tb_transport_route")
@Schema(name = "TransportRouteEntity", description = "公交线路")
public class TransportRouteEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "线路业务编码")
    @TableField("route_code")
    private String routeCode;

    @Schema(description = "目的地ID")
    @TableField("destination_id")
    private Long destinationId;

    @Schema(description = "纬度（发布选点）")
    @TableField("latitude")
    private java.math.BigDecimal latitude;

    @Schema(description = "经度（发布选点）")
    @TableField("longitude")
    private java.math.BigDecimal longitude;

    @Schema(description = "线路名称")
    @TableField("name")
    private String name;

    @Schema(description = "首班时间")
    @TableField("first_bus")
    private String firstBus;

    @Schema(description = "末班时间")
    @TableField("last_bus")
    private String lastBus;

    @Schema(description = "起点站")
    @TableField("start_station")
    private String startStation;

    @Schema(description = "终点站")
    @TableField("end_station")
    private String endStation;

    @Schema(description = "状态：1上架 0下架")
    @TableField("status")
    private Integer status;

    @Schema(description = "运行状态键")
    @TableField("status_key")
    private String statusKey;

    @Schema(description = "运行状态文案")
    @TableField("status_label")
    private String statusLabel;

    @Schema(description = "主题色")
    @TableField("accent_color")
    private String accentColor;

    @Schema(description = "浅色主题")
    @TableField("accent_light")
    private String accentLight;

    @Schema(description = "是否展示实时站点")
    @TableField("show_realtime_stops")
    private Integer showRealtimeStops;

    @Schema(description = "是否可展开站点")
    @TableField("expandable")
    private Integer expandable;

    @Schema(description = "底部类型")
    @TableField("footer_type")
    private String footerType;

    @Schema(description = "当前站序号")
    @TableField("current_stop_index")
    private Integer currentStopIndex;

    @Schema(description = "当前站名称")
    @TableField("current_stop")
    private String currentStop;

    @Schema(description = "下一站名称")
    @TableField("next_stop")
    private String nextStop;

    @Schema(description = "预计到站")
    @TableField("next_eta")
    private String nextEta;

    @Schema(description = "上下行方向 JSON")
    @TableField("directions_json")
    private String directionsJson;

    @Schema(description = "实时车辆 JSON")
    @TableField("track_info_json")
    private String trackInfoJson;

    @Schema(description = "实时动态 JSON")
    @TableField("realtime_updates_json")
    private String realtimeUpdatesJson;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

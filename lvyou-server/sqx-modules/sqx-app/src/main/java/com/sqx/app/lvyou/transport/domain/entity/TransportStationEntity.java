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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 公交站点
 */
@Getter
@Setter
@TableName("tb_transport_station")
@Schema(name = "TransportStationEntity", description = "公交站点")
public class TransportStationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "线路ID")
    @TableField("route_id")
    private Long routeId;

    @Schema(description = "站点名称")
    @TableField("station_name")
    private String stationName;

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "站点顺序")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

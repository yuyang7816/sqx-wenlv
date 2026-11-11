package com.sqx.app.lvyou.route.domain.entity;

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
 * 线路行程节点
 */
@Getter
@Setter
@TableName("tb_route_itinerary")
@Schema(name = "RouteItineraryEntity", description = "线路行程节点")
public class RouteItineraryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "线路ID")
    @TableField("route_id")
    private Long routeId;

    @Schema(description = "节点顺序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "所属行程天序号")
    @TableField("day_no")
    private Integer dayNo;

    @Schema(description = "时间节点")
    @TableField("time_label")
    private String timeLabel;

    @Schema(description = "节点名称")
    @TableField("name")
    private String name;

    @Schema(description = "地点")
    @TableField("location")
    private String location;

    @Schema(description = "节点描述")
    @TableField("description")
    private String description;

    @Schema(description = "封面图")
    @TableField("cover_image")
    private String coverImage;

    @Schema(description = "节点图片 JSON 数组")
    @TableField("images_json")
    private String imagesJson;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

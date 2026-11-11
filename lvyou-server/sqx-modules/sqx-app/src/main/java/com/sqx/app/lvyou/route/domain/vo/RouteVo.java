package com.sqx.app.lvyou.route.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端线路
 */
@Data
@Schema(description = "管理端线路")
public class RouteVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "目的地名称")
    private String destinationName;

    @Schema(description = "线路名称")
    private String name;

    @Schema(description = "短描述")
    private String description;

    @Schema(description = "详细介绍")
    private String intro;

    @Schema(description = "起步价")
    private BigDecimal priceFrom;

    @Schema(description = "行程时长")
    private String duration;

    @Schema(description = "集合地点")
    private String meetPoint;

    @Schema(description = "途经景点")
    private String spots;

    @Schema(description = "标签JSON")
    private String tags;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "行程节点")
    private List<RouteItineraryVo> itineraryList;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

package com.sqx.app.lvyou.route.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端新增线路
 */
@Data
@Schema(description = "新增线路")
public class RouteSaveBo {

    @Schema(description = "商户ID")
    @NotNull(message = "商户ID不能为空")
    private Long merchantId;

    @Schema(description = "目的地ID（服务端按商户/经纬度自动解析，客户端勿传）")
    private Long destinationId;

    @Schema(description = "线路名称")
    @NotBlank(message = "线路名称不能为空")
    private String name;

    @Schema(description = "短描述")
    private String description;

    @Schema(description = "详细介绍")
    private String intro;

    @Schema(description = "起步价")
    @NotNull(message = "起步价不能为空")
    private BigDecimal priceFrom;

    @Schema(description = "行程时长")
    private String duration;

    @Schema(description = "集合地点")
    private String meetPoint;

    @Schema(description = "途经景点（逗号分隔或列表）")
    private String spots;

    @Schema(description = "标签JSON")
    private String tags;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "状态：1上架 0下架")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "行程节点")
    @Valid
    private List<RouteItinerarySaveBo> itineraryList;
}

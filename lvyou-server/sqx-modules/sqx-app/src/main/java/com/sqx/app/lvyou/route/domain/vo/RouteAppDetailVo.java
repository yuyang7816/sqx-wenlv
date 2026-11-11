package com.sqx.app.lvyou.route.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端线路详情
 */
@Data
@Schema(description = "用户端线路详情")
public class RouteAppDetailVo {

    @Schema(description = "线路ID")
    private Long id;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "线路名称")
    private String name;

    @Schema(description = "短描述")
    private String desc;

    @Schema(description = "详细介绍")
    private String intro;

    @Schema(description = "起步价")
    private BigDecimal priceFrom;

    @Schema(description = "预约单价")
    private BigDecimal price;

    @Schema(description = "行程时长")
    private String duration;

    @Schema(description = "行程天数说明")
    private String days;

    @Schema(description = "行程天数")
    private Integer dayCount;

    @Schema(description = "旅行城市")
    private String travelCity;

    @Schema(description = "集合地点")
    private String meetPoint;

    @Schema(description = "出行日期规则说明")
    private String tripDateRule;

    @Schema(description = "标签列表")
    private List<RouteTagVo> tags;

    @Schema(description = "途经景点")
    private List<String> spots;

    @Schema(description = "封面图")
    private String cover;

    @Schema(description = "轮播图")
    private List<String> banners;

    @Schema(description = "商户微信联系二维码")
    private String wechatQrCode;

    @Schema(description = "商户联系电话")
    private String contactPhone;

    @Schema(description = "门店封面")
    private String storeCover;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "门店标签")
    private List<String> storeTags;

    @Schema(description = "门店地址")
    private String storeAddress;

    @Schema(description = "门店纬度")
    private BigDecimal storeLatitude;

    @Schema(description = "门店经度")
    private BigDecimal storeLongitude;

    @Schema(description = "营业时间")
    private String storeHours;

    @Schema(description = "门店/导游介绍")
    private String storeIntro;

    @Schema(description = "门店轮播图")
    private List<String> storeBanners;

    @Schema(description = "导游团队")
    private List<RouteGuideAppVo> guides;

    @Schema(description = "行程预览")
    private List<RouteItineraryAppVo> itinerary;

    @Schema(description = "按天行程")
    private List<RouteItineraryDayAppVo> itineraryDays;
}

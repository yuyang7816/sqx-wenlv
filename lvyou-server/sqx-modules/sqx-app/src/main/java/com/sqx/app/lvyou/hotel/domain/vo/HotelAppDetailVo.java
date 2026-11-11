package com.sqx.app.lvyou.hotel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端酒店详情
 */
@Data
@Schema(description = "用户端酒店详情")
public class HotelAppDetailVo {

    @Schema(description = "酒店ID")
    private Long id;

    @Schema(description = "酒店名称")
    private String name;

    @Schema(description = "星级")
    private Integer starLevel;

    @Schema(description = "星级文案")
    private String starLabel;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "评分")
    private BigDecimal score;

    @Schema(description = "标签")
    private String tag;

    @Schema(description = "起步价")
    private BigDecimal priceFrom;

    @Schema(description = "封面图")
    private String bannerImage;

    @Schema(description = "轮播图列表")
    private List<String> bannerImages;

    @Schema(description = "入住时间说明")
    private String checkInTime;

    @Schema(description = "退房时间说明")
    private String checkOutTime;

    @Schema(description = "设施列表")
    private List<HotelFacilityVo> facilities;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "酒店政策")
    private HotelPolicyVo policy;

    @Schema(description = "房型列表")
    private List<HotelRoomAppVo> roomList;
}

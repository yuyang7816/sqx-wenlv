package com.sqx.app.lvyou.hotel.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 管理端新增酒店
 */
@Data
@Schema(description = "新增酒店")
public class HotelSaveBo {

    @Schema(description = "商户ID")
    @NotNull(message = "商户ID不能为空")
    private Long merchantId;

    @Schema(description = "目的地ID（服务端按经纬度/商户自动解析，客户端勿传）")
    private Long destinationId;

    @Schema(description = "酒店名称")
    @NotBlank(message = "酒店名称不能为空")
    private String name;

    @Schema(description = "分类：luxury/boutique/business/resort")
    private String category;

    @Schema(description = "星级")
    private Integer starLevel;

    @Schema(description = "酒店地址（地图选点回填，勿手填）")
    private String address;

    @Schema(description = "发布位置纬度（地图选点）")
    private BigDecimal latitude;

    @Schema(description = "发布位置经度（地图选点）")
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
    private String coverImage;

    @Schema(description = "轮播图JSON")
    private String bannerImages;

    @Schema(description = "入住时间说明")
    private String checkInTime;

    @Schema(description = "退房时间说明")
    private String checkOutTime;

    @Schema(description = "设施JSON")
    private String facilities;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "状态：1上架 0下架")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;
}

package com.sqx.app.lvyou.hotel.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端酒店
 */
@Data
@Schema(description = "管理端酒店")
public class HotelVo {

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

    @Schema(description = "酒店名称")
    private String name;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "分类文案")
    private String categoryLabel;

    @Schema(description = "星级")
    private Integer starLevel;

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

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

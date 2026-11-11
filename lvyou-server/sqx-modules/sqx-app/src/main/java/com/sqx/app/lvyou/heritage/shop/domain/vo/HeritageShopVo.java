package com.sqx.app.lvyou.heritage.shop.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端非遗店铺
 */
@Data
@Schema(description = "管理端非遗店铺")
public class HeritageShopVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "目的地名称")
    private String destinationName;

    @Schema(description = "关联非遗工艺ID")
    private Long craftId;

    @Schema(description = "关联工艺名称")
    private String craftName;

    @Schema(description = "店铺名称")
    private String name;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "纬度（地图选点）")
    private BigDecimal latitude;

    @Schema(description = "经度（地图选点）")
    private BigDecimal longitude;

    @Schema(description = "主标签")
    private String tag;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "分类：pottery/textile/culture")
    private String category;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "详细介绍")
    private String introDetail;

    @Schema(description = "列表短描述")
    private String description;

    @Schema(description = "封面渐变起始色")
    private String coverFrom;

    @Schema(description = "封面渐变结束色")
    private String coverTo;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "营业时间")
    private String businessHours;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "评分")
    private BigDecimal rating;

    @Schema(description = "评价数")
    private Integer reviewCount;

    @Schema(description = "服务项目列表")
    private List<String> services;

    @Schema(description = "状态：1营业 0歇业")
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

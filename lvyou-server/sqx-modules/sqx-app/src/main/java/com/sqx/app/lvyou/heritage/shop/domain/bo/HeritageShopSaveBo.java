package com.sqx.app.lvyou.heritage.shop.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端新增非遗店铺
 */
@Data
@Schema(description = "新增非遗店铺")
public class HeritageShopSaveBo {

    @Schema(description = "目的地ID（服务端按经纬度自动解析）")
    private Long destinationId;

    @Schema(description = "发布位置纬度")
    private BigDecimal latitude;

    @Schema(description = "发布位置经度")
    private BigDecimal longitude;

    @Schema(description = "关联非遗工艺ID")
    private Long craftId;

    @Schema(description = "店铺名称")
    @NotBlank(message = "店铺名称不能为空")
    private String name;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "主标签")
    private String tag;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "好店分类编码（对齐 tb_heritage_shop_category.code）")
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
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;
}

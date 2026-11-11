package com.sqx.app.lvyou.scenic.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端景区
 */
@Data
@Schema(description = "管理端景区")
public class ScenicVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "目的地名称")
    private String destinationName;

    @Schema(description = "景区名称")
    private String name;

    @Schema(description = "景区地址")
    private String address;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "开放时间")
    private String openTime;

    @Schema(description = "咨询电话")
    private String phone;

    @Schema(description = "景区等级")
    private String levelLabel;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "分类文案")
    private String categoryLabel;

    @Schema(description = "景区特色介绍")
    private String intro;

    @Schema(description = "特色配图列表")
    private List<String> featureImages;

    @Schema(description = "轮播图列表")
    private List<String> bannerImages;

    @Schema(description = "游玩提示")
    private List<String> tips;

    @Schema(description = "门票起步价")
    private BigDecimal priceFrom;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "状态：1上架 0下架")
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

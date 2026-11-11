package com.sqx.app.lvyou.scenic.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端新增景区
 */
@Data
@Schema(description = "新增景区")
public class ScenicSaveBo {

    @Schema(description = "目的地ID（服务端按经纬度自动解析，客户端勿传）")
    private Long destinationId;

    @Schema(description = "景区名称")
    @NotBlank(message = "景区名称不能为空")
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

    @Schema(description = "分类编码（来自景区分类管理）")
    private String category;

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
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;
}

package com.sqx.app.lvyou.scenic.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端景区详情
 */
@Data
@Schema(description = "用户端景区详情")
public class ScenicAppDetailVo {

    @Schema(description = "景区ID")
    private Long id;

    @Schema(description = "景区名称")
    private String name;

    @Schema(description = "门票展示文案")
    private String ticketPrice;

    @Schema(description = "是否免费")
    private Boolean ticketFree;

    @Schema(description = "景区等级")
    private String levelLabel;

    @Schema(description = "景区地址")
    private String address;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "咨询电话")
    private String phone;

    @Schema(description = "开放时间")
    private String openTime;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "封面图")
    private String cover;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "轮播图")
    private List<String> bannerImages;

    @Schema(description = "首图（兼容字段）")
    private String bannerImage;

    @Schema(description = "特色配图列表")
    private List<String> featureImages;

    @Schema(description = "特色配图（首图，兼容字段）")
    private String featureImage;

    @Schema(description = "景区特色介绍")
    private String featureDesc;

    @Schema(description = "结构化标签")
    private List<ScenicTagVo> tags;

    @Schema(description = "游玩提示")
    private List<String> tips;

    @Schema(description = "门票起步价")
    private BigDecimal priceFrom;
}

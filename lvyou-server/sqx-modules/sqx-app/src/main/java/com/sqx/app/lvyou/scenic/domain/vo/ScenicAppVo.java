package com.sqx.app.lvyou.scenic.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端景区列表项
 */
@Data
@Schema(description = "用户端景区列表项")
public class ScenicAppVo {

    @Schema(description = "景区ID")
    private Long id;

    @Schema(description = "景区名称")
    private String name;

    @Schema(description = "景区等级")
    private String levelLabel;

    @Schema(description = "景区地址")
    private String address;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "分类文案")
    private String categoryLabel;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "门票起步价")
    private BigDecimal priceFrom;

    @Schema(description = "封面图")
    private String cover;

    @Schema(description = "封面图")
    private String coverImage;
}

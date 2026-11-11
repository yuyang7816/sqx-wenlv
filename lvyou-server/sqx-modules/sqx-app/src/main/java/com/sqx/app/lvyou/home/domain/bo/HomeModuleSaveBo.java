package com.sqx.app.lvyou.home.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 新增首页模块
 */
@Data
public class HomeModuleSaveBo {

    @NotBlank(message = "模块类型不能为空")
    @Schema(description = "grid/tab")
    private String moduleType;

    @Schema(description = "业务键，留空则自动生成")
    private String moduleKey;

    @NotBlank(message = "展示名称不能为空")
    private String label;

    private String icon;
    private String iconColor;
    private String bgColor;
    private String linkPath;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "适用目的地（服务端按经纬度自动解析，未匹配则全平台）")
    private Long destinationId;

    @Schema(description = "发布位置纬度")
    private BigDecimal latitude;

    @Schema(description = "发布位置经度")
    private BigDecimal longitude;
}

package com.sqx.app.lvyou.home.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端首页模块 VO
 */
@Data
@Schema(name = "HomeModuleVo", description = "首页模块")
public class HomeModuleVo {

    private Long id;

    @Schema(description = "模块类型：grid/tab")
    private String moduleType;

    @Schema(description = "模块类型文案")
    private String moduleTypeLabel;

    @Schema(description = "业务键")
    private String moduleKey;

    @Schema(description = "展示名称")
    private String label;

    private String icon;
    private String iconColor;
    private String bgColor;
    private String linkPath;
    private Integer sort;
    private Integer status;

    @Schema(description = "状态文案")
    private String statusLabel;

    private Long destinationId;

    @Schema(description = "目的地名称")
    private String destinationName;

    private String createTime;
}

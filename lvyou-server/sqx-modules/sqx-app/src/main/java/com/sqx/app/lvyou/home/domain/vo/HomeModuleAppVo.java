package com.sqx.app.lvyou.home.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端首页模块 VO
 */
@Data
@Schema(name = "HomeModuleAppVo", description = "用户端首页模块")
public class HomeModuleAppVo {

    @Schema(description = "业务键")
    private String key;

    @Schema(description = "展示名称")
    private String label;

    private String icon;
    private String iconColor;
    private String bgColor;

    @Schema(description = "跳转路径")
    private String path;
}

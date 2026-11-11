package com.sqx.app.base.map.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端地图选点配置
 */
@Data
@Schema(description = "地图选点配置")
public class MapPickerConfigVo {

    @Schema(description = "地图平台：tencent/tianditu/google")
    private String platform;

    @Schema(description = "腾讯地图 Key")
    private String tencentKey;

    @Schema(description = "Referer 标识")
    private String referer;
}

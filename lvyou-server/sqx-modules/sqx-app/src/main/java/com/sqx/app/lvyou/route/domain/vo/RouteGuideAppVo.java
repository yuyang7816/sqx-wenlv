package com.sqx.app.lvyou.route.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端线路详情 - 导游
 */
@Data
@Schema(description = "用户端线路导游")
public class RouteGuideAppVo {

    @Schema(description = "导游名称")
    private String name;

    @Schema(description = "形象照")
    private String avatar;
}

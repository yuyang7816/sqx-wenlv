package com.sqx.app.lvyou.transport.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 附近公交站点
 */
@Data
@Schema(description = "附近公交站点")
public class TransportNearbyStationVo {

    @Schema(description = "站点名称")
    private String name;

    @Schema(description = "与当前位置距离（公里）")
    private BigDecimal distanceKm;
}

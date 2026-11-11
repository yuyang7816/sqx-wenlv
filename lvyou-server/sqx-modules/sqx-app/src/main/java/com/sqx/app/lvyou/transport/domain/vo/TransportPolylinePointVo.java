package com.sqx.app.lvyou.transport.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 公交线路轨迹点
 */
@Data
@Schema(description = "公交线路轨迹点")
public class TransportPolylinePointVo {

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "站点名称")
    private String stationName;
}

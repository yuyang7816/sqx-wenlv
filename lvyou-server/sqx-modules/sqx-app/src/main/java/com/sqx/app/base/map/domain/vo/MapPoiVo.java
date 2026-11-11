package com.sqx.app.base.map.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 地图 POI 列表项
 */
@Data
@Schema(description = "地图POI")
public class MapPoiVo {

    @Schema(description = "POI唯一标识，格式：{type}-{sourceId}")
    private String id;

    @Schema(description = "POI类型：scenic/food/parking/transport")
    private String type;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "与参考点距离（公里），传入 lat/lng 时计算")
    private BigDecimal distance;

    @Schema(description = "标签")
    private String tag;

    @Schema(description = "数据源类型：scenic/merchant/map_poi")
    private String sourceType;

    @Schema(description = "数据源业务ID")
    private Long sourceId;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "剩余车位（停车类）")
    private Integer spaces;

    @Schema(description = "总车位（停车类）")
    private Integer totalSpaces;

    @Schema(description = "收费说明（停车类）")
    private String feeDesc;
}

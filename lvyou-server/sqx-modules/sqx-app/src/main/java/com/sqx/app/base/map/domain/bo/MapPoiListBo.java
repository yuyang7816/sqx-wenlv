package com.sqx.app.base.map.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 地图 POI 分页查询
 */
@Data
@Schema(description = "地图POI分页查询")
public class MapPoiListBo {

    @Schema(description = "页码")
    private Long page;

    @Schema(description = "每页条数")
    private Long limit;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "POI类型：scenic/food/parking/transport")
    private String type;

    @Schema(description = "参考点纬度（用于距离排序）")
    private String lat;

    @Schema(description = "参考点经度（用于距离排序）")
    private String lng;

    @Schema(description = "名称关键词（模糊）")
    private String keyword;
}

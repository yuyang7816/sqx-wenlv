package com.sqx.app.lvyou.transport.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端公交线路分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户端公交线路分页查询")
public class TransportRouteAppListBo extends PageParam {

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "纬度 GCJ-02")
    private String lat;

    @Schema(description = "经度 GCJ-02")
    private String lng;

    @Schema(description = "城市（uni.chooseLocation 解析，如「长沙市」）")
    private String city;

    @Schema(description = "区县（uni.chooseLocation 解析，如「岳麓区」）")
    private String district;

    @Schema(description = "详细地址（uni.chooseLocation.address）")
    private String address;

    @Schema(description = "位置名称（uni.chooseLocation.name，如站点/POI 名）")
    private String locationName;

    @Schema(description = "线路名称或站点关键词（模糊）")
    private String keyword;
}

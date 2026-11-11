package com.sqx.app.lvyou.hotel.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端酒店列表查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户端酒店列表查询")
public class HotelAppListBo extends PageParam {

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "分类：luxury/boutique/business/resort，all 或不传表示全部")
    private String category;

    @Schema(description = "排序：featured 精选 / distance 距离 / price 价格")
    private String sort;

    @Schema(description = "价格排序：asc 低到高 / desc 高到低（sort=price 时生效）")
    private String priceOrder;

    @Schema(description = "距离排序：asc 最近 / desc 最远（sort=distance 时生效）")
    private String distanceOrder;

    @Schema(description = "用户纬度（GCJ-02，距离排序时使用）")
    private String lat;

    @Schema(description = "用户经度（GCJ-02，距离排序时使用）")
    private String lng;
}

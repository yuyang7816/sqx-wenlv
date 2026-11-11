package com.sqx.app.lvyou.order.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 物流轨迹节点
 */
@Data
@Schema(description = "物流轨迹节点")
public class OrderLogisticsTrackVo {

    @Schema(description = "节点时间")
    private String time;

    @Schema(description = "节点描述")
    private String context;

    @Schema(description = "节点状态")
    private String status;
}

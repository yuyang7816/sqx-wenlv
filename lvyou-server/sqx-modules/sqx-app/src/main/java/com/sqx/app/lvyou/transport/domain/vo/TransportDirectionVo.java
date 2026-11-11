package com.sqx.app.lvyou.transport.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公交上下行方向
 */
@Data
@Schema(description = "公交上下行方向")
public class TransportDirectionVo {

    @Schema(description = "方向标签，如 上行/下行")
    private String label;

    @Schema(description = "起点站")
    private String from;

    @Schema(description = "终点站")
    private String to;

    @Schema(description = "首班时间")
    private String firstBus;

    @Schema(description = "末班时间")
    private String lastBus;

    @Schema(description = "发车间隔")
    private String interval;
}

package com.sqx.app.lvyou.transport.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端公交时刻表
 */
@Data
@Schema(description = "用户端公交时刻表")
public class TransportScheduleAppVo {

    @Schema(description = "当前线路业务编码")
    private String id;

    @Schema(description = "当前线路名称")
    private String name;

    @Schema(description = "可选线路列表")
    private List<TransportRouteBriefVo> routes;

    @Schema(description = "上下行方向")
    private List<TransportDirectionVo> directions;

    @Schema(description = "时刻表行")
    private List<TransportScheduleRowVo> scheduleRows;
}

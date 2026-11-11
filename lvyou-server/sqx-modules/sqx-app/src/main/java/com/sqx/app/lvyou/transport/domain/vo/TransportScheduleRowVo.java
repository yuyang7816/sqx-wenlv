package com.sqx.app.lvyou.transport.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 公交时刻表行
 */
@Data
@Schema(description = "公交时刻表行")
public class TransportScheduleRowVo {

    @Schema(description = "站点名称")
    private String station;

    @Schema(description = "到站时刻列表")
    private List<String> times;
}

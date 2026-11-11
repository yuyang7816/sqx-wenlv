package com.sqx.app.lvyou.transport.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公交实时动态
 */
@Data
@Schema(description = "公交实时动态")
public class TransportRealtimeUpdateVo {

    @Schema(description = "动态ID")
    private String id;

    @Schema(description = "标签")
    private String label;

    @Schema(description = "内容")
    private String value;

    @Schema(description = "时间文案")
    private String time;
}

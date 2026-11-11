package com.sqx.app.lvyou.transport.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公交线路简要信息（时刻表页切换用）
 */
@Data
@Schema(description = "公交线路简要信息")
public class TransportRouteBriefVo {

    @Schema(description = "线路业务编码")
    private String id;

    @Schema(description = "线路名称")
    private String name;
}

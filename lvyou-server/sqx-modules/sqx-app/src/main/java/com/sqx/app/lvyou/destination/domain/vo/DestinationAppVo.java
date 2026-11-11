package com.sqx.app.lvyou.destination.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端目的地摘要
 */
@Data
@Schema(description = "用户端目的地摘要")
public class DestinationAppVo {

    @Schema(description = "目的地业务编码（对外主键）")
    private String id;

    @Schema(description = "目的地全称")
    private String name;

    @Schema(description = "简称")
    private String shortName;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "状态文案：已上线/即将上线/禁用")
    private String status;

    @Schema(description = "是否默认目的地")
    private Boolean defaultDestination;

    @Schema(description = "商户数量")
    private Integer merchantCount;

    @Schema(description = "资源数量")
    private Integer resourceCount;
}

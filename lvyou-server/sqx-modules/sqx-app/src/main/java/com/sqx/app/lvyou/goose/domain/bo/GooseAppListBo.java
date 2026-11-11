package com.sqx.app.lvyou.goose.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 灰鹅美食列表查询
 */
@Getter
@Setter
@Schema(description = "灰鹅美食列表查询")
public class GooseAppListBo {

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "门店名称关键词")
    private String keyword;

    @Schema(description = "页码，从1开始")
    private Long page;

    @Schema(description = "每页条数")
    private Long limit;
}

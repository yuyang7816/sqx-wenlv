package com.sqx.app.lvyou.scenic.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端景区列表查询
 */
@Data
@Schema(description = "用户端景区列表查询")
public class ScenicAppListBo {

    @Schema(description = "页码")
    private Long page;

    @Schema(description = "每页条数")
    private Long limit;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "景区名称（模糊）")
    private String name;

    @Schema(description = "分类：all/culture/nature/red/folk")
    private String category;
}

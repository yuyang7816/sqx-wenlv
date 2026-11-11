package com.sqx.app.lvyou.scenic.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端景区分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "景区分页查询")
public class ScenicPageBo extends PageParam {

    @Schema(description = "景区名称（模糊）")
    private String name;

    @Schema(description = "关联目的地ID")
    private Long destinationId;

    @Schema(description = "分类：culture/nature/red/folk")
    private String category;

    @Schema(description = "景区等级（模糊）")
    private String levelLabel;

    @Schema(description = "状态：1上架 0下架")
    private Integer status;
}

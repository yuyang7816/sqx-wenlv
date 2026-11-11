package com.sqx.app.lvyou.activity.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端活动分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "活动分页查询")
public class ActivityPageBo extends PageParam {

    @Schema(description = "活动名称（模糊）")
    private String name;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "活动阶段：ongoing/upcoming/ended")
    private String phase;

    @Schema(description = "状态：1上架 0下架")
    private Integer status;
}

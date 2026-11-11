package com.sqx.app.lvyou.activity.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 活动上下架
 */
@Data
@Schema(description = "活动上下架")
public class ActivityUpdateStateBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "状态：1上架 0下架")
    @NotNull(message = "状态不能为空")
    private Integer status;
}

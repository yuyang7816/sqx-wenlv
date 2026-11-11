package com.sqx.app.lvyou.activity.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 活动主键操作
 */
@Data
@Schema(description = "活动主键")
public class ActivityInfoBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;
}

package com.sqx.app.lvyou.destination.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理端更新目的地状态
 */
@Data
@Schema(description = "更新目的地状态")
public class DestinationUpdateStateBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "状态：1已上线 2即将上线 0禁用")
    @NotNull(message = "状态不能为空")
    private Integer status;
}

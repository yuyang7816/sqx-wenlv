package com.sqx.app.lvyou.home.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 首页模块启用/禁用
 */
@Data
@Schema(description = "首页模块启用/禁用")
public class HomeModuleUpdateStateBo {

    @NotNull(message = "id不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    @Schema(description = "1启用 0禁用")
    private Integer status;
}

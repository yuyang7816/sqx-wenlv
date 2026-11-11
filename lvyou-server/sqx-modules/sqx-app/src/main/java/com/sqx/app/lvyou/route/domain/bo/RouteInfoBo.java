package com.sqx.app.lvyou.route.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理端线路主键操作
 */
@Data
@Schema(description = "线路主键操作")
public class RouteInfoBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;
}

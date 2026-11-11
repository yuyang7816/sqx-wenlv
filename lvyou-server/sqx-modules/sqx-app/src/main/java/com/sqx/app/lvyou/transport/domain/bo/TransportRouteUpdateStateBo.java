package com.sqx.app.lvyou.transport.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理端公交线路上下架
 */
@Data
@Schema(description = "公交线路上下架")
public class TransportRouteUpdateStateBo {

    @Schema(description = "主键")
    @NotNull(message = "线路ID不能为空")
    private Long id;

    @Schema(description = "状态：1上架 0下架")
    @NotNull(message = "状态不能为空")
    private Integer status;
}

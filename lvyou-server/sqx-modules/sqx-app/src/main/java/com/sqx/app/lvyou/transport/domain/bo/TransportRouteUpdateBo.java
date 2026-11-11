package com.sqx.app.lvyou.transport.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 管理端更新公交线路
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新公交线路")
public class TransportRouteUpdateBo extends TransportRouteSaveBo {

    @Schema(description = "主键")
    @NotNull(message = "线路ID不能为空")
    private Long id;
}

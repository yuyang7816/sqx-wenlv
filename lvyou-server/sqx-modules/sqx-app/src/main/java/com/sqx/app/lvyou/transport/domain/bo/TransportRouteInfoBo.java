package com.sqx.app.lvyou.transport.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理端公交线路详情/删除
 */
@Data
@Schema(description = "公交线路详情/删除")
public class TransportRouteInfoBo {

    @Schema(description = "主键")
    @NotNull(message = "线路ID不能为空")
    private Long id;
}

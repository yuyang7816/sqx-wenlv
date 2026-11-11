package com.sqx.app.lvyou.transport.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端公交线路分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理端公交线路分页查询")
public class TransportRoutePageBo extends PageParam {

    @Schema(description = "线路ID")
    private Long id;

    @Schema(description = "线路业务编码（模糊）")
    private String routeCode;

    @Schema(description = "线路名称（模糊）")
    private String name;

    @Schema(description = "起点站（模糊）")
    private String startStation;

    @Schema(description = "终点站（模糊）")
    private String endStation;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "状态：1上架 0下架")
    private Integer status;
}

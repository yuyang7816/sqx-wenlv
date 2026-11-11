package com.sqx.app.lvyou.route.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端线路分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "线路分页查询")
public class RoutePageBo extends PageParam {

    @Schema(description = "线路名称（模糊）")
    private String name;

    @Schema(description = "关联目的地ID")
    private Long destinationId;

    @Schema(description = "关联商户名称（模糊）")
    private String merchantName;

    @Schema(description = "状态：1上架 0下架")
    private Integer status;
}

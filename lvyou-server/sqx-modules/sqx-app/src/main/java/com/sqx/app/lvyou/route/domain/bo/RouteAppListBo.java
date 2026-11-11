package com.sqx.app.lvyou.route.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 用户端线路列表查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户端线路列表查询")
public class RouteAppListBo extends PageParam {

    @Schema(description = "目的地ID（单点；与 destinationIds 二选一）")
    private Long destinationId;

    @Schema(description = "市级范围内目的地 ID 列表（优先）")
    private List<Long> destinationIds;

    @Schema(description = "线路名称（模糊）")
    private String name;

    @Schema(description = "商户ID（按门店查线路时使用，传入后不再按目的地过滤）")
    private Long merchantId;

    @Schema(description = "行程天数精确匹配（与 minDayCount 二选一）")
    private Integer dayCount;

    @Schema(description = "行程天数下限（含），如多日游传 3")
    private Integer minDayCount;
}

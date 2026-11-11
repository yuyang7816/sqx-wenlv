package com.sqx.app.lvyou.route.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端线路门店评价列表查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "线路门店评价列表查询")
public class RouteReviewAppListBo extends PageParam {

    @Schema(description = "旅行社商户ID")
    private Long merchantId;
}

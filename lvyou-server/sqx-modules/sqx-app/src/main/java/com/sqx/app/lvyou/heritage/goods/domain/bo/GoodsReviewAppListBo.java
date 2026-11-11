package com.sqx.app.lvyou.heritage.goods.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端商品评价列表查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "商品评价列表查询")
public class GoodsReviewAppListBo extends PageParam {

    @Schema(description = "商品ID")
    private Long goodsId;
}

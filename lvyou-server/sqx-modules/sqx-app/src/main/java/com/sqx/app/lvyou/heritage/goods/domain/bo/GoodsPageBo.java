package com.sqx.app.lvyou.heritage.goods.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端非遗商品分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "非遗商品分页查询")
public class GoodsPageBo extends PageParam {

    @Schema(description = "商品名称（模糊）")
    private String name;

    @Schema(description = "店铺名称（模糊，关联查询）")
    private String shopName;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "分类：featured/hot/new")
    private String category;

    @Schema(description = "状态：1上架 0下架")
    private Integer status;
}

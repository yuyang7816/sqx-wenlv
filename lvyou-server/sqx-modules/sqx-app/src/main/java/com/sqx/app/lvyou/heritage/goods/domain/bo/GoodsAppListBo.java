package com.sqx.app.lvyou.heritage.goods.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端非遗商品列表查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户端非遗商品列表查询")
public class GoodsAppListBo extends PageParam {

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "商品名称（模糊）")
    private String name;

    @Schema(description = "分类（管理端维护字段，用户端列表请用 sort）")
    private String category;

    @Schema(description = "排序：all 默认 / hot 销量 / new 上架时间 / price 价格")
    private String sort;

    @Schema(description = "价格排序：asc 低到高 / desc 高到低（sort=price 时生效）")
    private String priceOrder;
}

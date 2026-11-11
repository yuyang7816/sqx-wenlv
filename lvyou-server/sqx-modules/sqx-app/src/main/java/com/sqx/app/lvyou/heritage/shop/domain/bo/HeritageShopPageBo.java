package com.sqx.app.lvyou.heritage.shop.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端非遗店铺分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "非遗店铺分页查询")
public class HeritageShopPageBo extends PageParam {

    @Schema(description = "店铺名称（模糊）")
    private String name;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "分类：pottery/textile/culture")
    private String category;

    @Schema(description = "关联工艺名称（模糊）")
    private String craftName;

    @Schema(description = "状态：1营业 0歇业")
    private Integer status;
}

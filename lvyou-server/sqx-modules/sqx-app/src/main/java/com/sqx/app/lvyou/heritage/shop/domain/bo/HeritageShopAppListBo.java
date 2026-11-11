package com.sqx.app.lvyou.heritage.shop.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端非遗店铺列表查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户端非遗店铺列表查询")
public class HeritageShopAppListBo extends PageParam {

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "店铺名称（模糊）")
    private String name;

    @Schema(description = "分类：pottery/textile/culture，all表示全部")
    private String category;
}

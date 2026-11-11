package com.sqx.app.lvyou.heritage.shop.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 非遗好店分类分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "非遗好店分类分页查询")
public class HeritageShopCategoryPageBo extends PageParam {

    @Schema(description = "分类名称（模糊）")
    private String name;

    @Schema(description = "分类编码（模糊）")
    private String code;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}

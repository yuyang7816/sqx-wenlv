package com.sqx.app.lvyou.scenic.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 景区分类分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "景区分类分页查询")
public class ScenicCategoryPageBo extends PageParam {

    @Schema(description = "分类名称（模糊）")
    private String name;

    @Schema(description = "分类编码（模糊）")
    private String code;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}

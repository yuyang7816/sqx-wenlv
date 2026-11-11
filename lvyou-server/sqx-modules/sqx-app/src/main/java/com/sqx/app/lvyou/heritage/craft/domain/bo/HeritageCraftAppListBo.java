package com.sqx.app.lvyou.heritage.craft.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端非遗工艺列表查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户端非遗工艺列表查询")
public class HeritageCraftAppListBo extends PageParam {

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "工艺名称（模糊）")
    private String name;

    @Schema(description = "工艺分类标签")
    private String tag;
}

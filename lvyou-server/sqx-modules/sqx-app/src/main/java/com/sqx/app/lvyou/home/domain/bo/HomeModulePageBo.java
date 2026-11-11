package com.sqx.app.lvyou.home.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 首页模块分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HomeModulePageBo extends PageParam {

    @Schema(description = "模块类型：grid/tab")
    private String moduleType;

    @Schema(description = "业务键")
    private String moduleKey;

    @Schema(description = "展示名称")
    private String label;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}

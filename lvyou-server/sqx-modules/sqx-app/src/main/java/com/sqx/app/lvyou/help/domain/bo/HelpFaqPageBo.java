package com.sqx.app.lvyou.help.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端 FAQ 分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "FAQ分页查询")
public class HelpFaqPageBo extends PageParam {

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "问题标题（模糊）")
    private String question;

    @Schema(description = "状态：1启用 0禁用")
    private Integer state;
}

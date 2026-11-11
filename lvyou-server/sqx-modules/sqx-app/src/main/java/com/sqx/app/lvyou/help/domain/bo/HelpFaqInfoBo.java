package com.sqx.app.lvyou.help.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * FAQ 主键操作
 */
@Data
@Schema(description = "FAQ主键")
public class HelpFaqInfoBo {

    @Schema(description = "主键")
    @NotNull(message = "ID不能为空")
    private Long id;
}

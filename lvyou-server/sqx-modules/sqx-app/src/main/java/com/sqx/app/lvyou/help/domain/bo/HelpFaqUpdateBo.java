package com.sqx.app.lvyou.help.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 管理端更新 FAQ
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新FAQ")
public class HelpFaqUpdateBo extends HelpFaqSaveBo {

    @Schema(description = "主键")
    @NotNull(message = "ID不能为空")
    private Long id;
}

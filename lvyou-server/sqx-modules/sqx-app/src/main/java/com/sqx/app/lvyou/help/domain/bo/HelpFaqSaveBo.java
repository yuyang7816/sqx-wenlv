package com.sqx.app.lvyou.help.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理端新增 FAQ
 */
@Data
@Schema(description = "新增FAQ")
public class HelpFaqSaveBo {

    @Schema(description = "分类编码：booking/account/payment/service")
    @NotBlank(message = "分类编码不能为空")
    private String categoryCode;

    @Schema(description = "问题标题")
    @NotBlank(message = "问题标题不能为空")
    private String question;

    @Schema(description = "问题答案")
    @NotBlank(message = "问题答案不能为空")
    private String answer;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：1启用 0禁用")
    @NotNull(message = "状态不能为空")
    private Integer state;
}

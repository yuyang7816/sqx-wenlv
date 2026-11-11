package com.sqx.app.lvyou.help.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端 FAQ 条目
 */
@Data
@Schema(description = "FAQ条目")
public class HelpFaqItemVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "问题标题")
    private String question;

    @Schema(description = "问题答案")
    private String answer;
}

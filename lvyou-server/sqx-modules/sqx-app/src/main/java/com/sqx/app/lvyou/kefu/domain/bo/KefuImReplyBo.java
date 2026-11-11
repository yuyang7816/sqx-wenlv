package com.sqx.app.lvyou.kefu.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Schema(description = "客服回复消息")
public class KefuImReplyBo {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID")
    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 500, message = "消息内容不能超过500字")
    @Schema(description = "消息内容")
    private String content;
}

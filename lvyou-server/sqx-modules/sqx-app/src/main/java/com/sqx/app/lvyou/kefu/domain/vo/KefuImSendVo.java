package com.sqx.app.lvyou.kefu.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发送消息结果")
public class KefuImSendVo {

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "会话ID")
    private Long sessionId;
}

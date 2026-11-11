package com.sqx.app.lvyou.kefu.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "在线客服消息")
public class KefuImMessageVo {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "发送方：user/staff")
    private String senderType;

    @Schema(description = "发送方文案")
    private String senderTypeLabel;

    @Schema(description = "发送人ID")
    private Long senderId;

    @Schema(description = "消息内容")
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发送时间")
    private LocalDateTime createTime;
}

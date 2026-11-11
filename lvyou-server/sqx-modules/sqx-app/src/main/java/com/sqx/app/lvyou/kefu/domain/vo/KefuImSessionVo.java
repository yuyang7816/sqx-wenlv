package com.sqx.app.lvyou.kefu.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "在线客服会话")
public class KefuImSessionVo {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "用户手机号")
    private String userPhone;

    @Schema(description = "会话状态")
    private String status;

    @Schema(description = "会话状态文案")
    private String statusLabel;

    @Schema(description = "最后一条消息")
    private String lastMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "客服未读数")
    private Integer adminUnreadCount;

    @Schema(description = "用户未读数")
    private Integer userUnreadCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

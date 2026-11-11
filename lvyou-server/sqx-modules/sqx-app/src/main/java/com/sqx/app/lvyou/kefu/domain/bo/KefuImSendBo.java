package com.sqx.app.lvyou.kefu.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "发送在线客服消息")
public class KefuImSendBo {

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 500, message = "消息内容不能超过500字")
    @Schema(description = "消息内容")
    private String content;
}

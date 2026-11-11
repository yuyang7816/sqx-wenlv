package com.sqx.app.lvyou.kefu.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "在线客服消息分页查询")
public class KefuImMessagePageBo extends PageParam {

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "拉取该ID之后的新消息（轮询用）")
    private Long sinceId;
}

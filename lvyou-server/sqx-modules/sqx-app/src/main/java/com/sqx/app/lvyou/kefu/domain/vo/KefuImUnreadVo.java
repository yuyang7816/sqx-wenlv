package com.sqx.app.lvyou.kefu.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "在线客服未读汇总")
public class KefuImUnreadVo {

    @Schema(description = "客服未读消息总数")
    private Integer count;
}

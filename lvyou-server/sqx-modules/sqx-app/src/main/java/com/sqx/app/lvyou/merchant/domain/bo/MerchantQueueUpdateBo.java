package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 餐饮现场排队状态更新
 */
@Getter
@Setter
@Schema(description = "餐饮现场排队状态更新")
public class MerchantQueueUpdateBo {

    @Schema(description = "当前是否排队")
    private Boolean queueEnabled;

    @Schema(description = "前面排队桌数（排队开启时 1-99）")
    private Integer queueTableCount;
}

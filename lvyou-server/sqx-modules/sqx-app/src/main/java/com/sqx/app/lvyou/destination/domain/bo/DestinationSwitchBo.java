package com.sqx.app.lvyou.destination.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户端切换目的地
 */
@Data
@Schema(description = "切换目的地")
public class DestinationSwitchBo {

    @Schema(description = "目的地业务编码")
    @NotBlank(message = "目的地编码不能为空")
    private String destinationCode;
}

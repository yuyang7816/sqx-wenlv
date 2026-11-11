package com.sqx.app.lvyou.order.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用户端删除收货地址
 */
@Data
@Schema(description = "删除收货地址")
public class AddressDeleteBo {

    @Schema(description = "地址ID")
    @NotNull(message = "地址ID不能为空")
    private Long id;
}

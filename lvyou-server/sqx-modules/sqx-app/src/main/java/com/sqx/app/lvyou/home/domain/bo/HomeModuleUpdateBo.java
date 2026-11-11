package com.sqx.app.lvyou.home.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 更新首页模块
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HomeModuleUpdateBo extends HomeModuleSaveBo {

    @NotNull(message = "ID不能为空")
    private Long id;
}

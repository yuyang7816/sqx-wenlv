package com.sqx.app.lvyou.home.domain.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 首页模块 ID 入参
 */
@Data
public class HomeModuleInfoBo {

    @NotNull(message = "ID不能为空")
    private Long id;
}

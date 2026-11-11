package com.sqx.app.base.user.domain.vo;

import com.sqx.app.base.user.domain.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/10/20
 */
@Data
public class UserVo extends UserEntity {

    @Schema(description = "钱包余额")
    private String balance;

    @Schema(description = "系统默认一级佣金比例")
    private String defaultCommissionRateLevel1;

    @Schema(description = "系统默认二级佣金比例")
    private String defaultCommissionRateLevel2;
}

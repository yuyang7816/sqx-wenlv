package com.sqx.app.base.user.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author chengming
 * @since 2025/4/24
 */
@Data
public class UpdateUserBO {

    /**
     * 用户ID
     */
    @Schema(description = "用户id")
    @NotNull(message = "用户id不能为空")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String userName;

    /**
     * 头像
     */
    @Schema(description = "头像")
    private String avatar;

    /**
     * 性别 1男 2女
     */
    @Schema(description = "性别 1男 2女")
    private Integer sex;

    /**
     * 年龄
     */
    @Schema(description = "年龄")
    private Integer age;

    /**
     * 微信小程序openid
     */
    @Schema(description = "微信小程序openid")

    private String openId;

    /**
     * 微信app openid
     */
    @Schema(description = "微信公众号openid")
    private String wxOpenId;

    /**
     * 密码
     */
    @Schema(description = "密码")
    private String password;

    /**
     * 苹果id
     */
    @Schema(description = "苹果id")
    private String appleId;

    /**
     * 手机类型 1安卓 2ios
     */
    @Schema(description = "手机类型 1安卓 2ios")
    private Integer sysPhone;

    /**
     * 状态 1正常 2禁用
     */
    @Schema(description = "状态 1正常 2禁用")
    private Integer status;

    /**
     * 来源 app 小程序 公众号
     */
    @Schema(description = "来源 app 小程序 公众号")
    private String platform;

    @Schema(description = "app推送id")
    private String clientId;

    /**
     * 邀请码
     */
    @Schema(description = "邀请码")
    private String invitationCode;

    @Schema(description = "钱包余额（管理员直接设置，兼容旧接口）")
    private String balance;

    @Schema(description = "钱包调整类型：increase增加 decrease减少")
    private String walletAdjustType;

    @Schema(description = "钱包调整金额（正数）")
    private String walletAdjustAmount;

    @Schema(description = "一级分佣比例，空字符串表示恢复系统默认")
    private String commissionRateLevel1;

    @Schema(description = "二级分佣比例，空字符串表示恢复系统默认")
    private String commissionRateLevel2;
}

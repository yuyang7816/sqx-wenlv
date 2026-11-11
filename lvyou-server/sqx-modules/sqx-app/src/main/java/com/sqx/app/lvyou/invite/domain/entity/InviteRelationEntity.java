package com.sqx.app.lvyou.invite.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邀请关系
 */
@Getter
@Setter
@TableName("tb_invite_relation")
@Schema(name = "InviteRelationEntity", description = "邀请关系")
public class InviteRelationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "被邀请用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "一级邀请人用户ID")
    @TableField("inviter_user_id")
    private Long inviterUserId;

    @Schema(description = "二级邀请人用户ID")
    @TableField("inviter_level2_user_id")
    private Long inviterLevel2UserId;

    @Schema(description = "注册时填写的邀请码")
    @TableField("invite_code")
    private String inviteCode;

    @Schema(description = "绑定时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

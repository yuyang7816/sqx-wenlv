package com.sqx.app.lvyou.favorite.domain.entity;

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
 * 用户浏览足迹
 */
@Getter
@Setter
@TableName("tb_footprint")
@Schema(name = "FootprintEntity", description = "用户浏览足迹")
public class FootprintEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "浏览对象类型")
    @TableField("target_type")
    private String targetType;

    @Schema(description = "浏览对象业务ID")
    @TableField("target_id")
    private Long targetId;

    @Schema(description = "最近浏览时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("visit_time")
    private LocalDateTime visitTime;

    @Schema(description = "首次浏览时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

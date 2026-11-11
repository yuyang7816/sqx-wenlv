package com.sqx.app.lvyou.destination.domain.entity;

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
 * 目的地
 */
@Getter
@Setter
@TableName("tb_destination")
@Schema(name = "DestinationEntity", description = "目的地")
public class DestinationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "目的地业务编码")
    @TableField("destination_code")
    private String destinationCode;

    @Schema(description = "目的地全称")
    @TableField("name")
    private String name;

    @Schema(description = "简称")
    @TableField("short_name")
    private String shortName;

    @Schema(description = "简介")
    @TableField("intro")
    private String intro;

    @Schema(description = "状态：1已上线 2即将上线 0禁用")
    @TableField("status")
    private Integer status;

    @Schema(description = "是否默认目的地：1是 0否")
    @TableField("is_default")
    private Integer isDefault;

    @Schema(description = "商户数量")
    @TableField("merchant_count")
    private Integer merchantCount;

    @Schema(description = "资源数量")
    @TableField("resource_count")
    private Integer resourceCount;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

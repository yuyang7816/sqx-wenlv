package com.sqx.app.lvyou.activity.domain.entity;

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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 精彩活动
 */
@Getter
@Setter
@TableName("tb_activity")
@Schema(name = "ActivityEntity", description = "精彩活动")
public class ActivityEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "目的地ID")
    @TableField("destination_id")
    private Long destinationId;

    @Schema(description = "活动名称")
    @TableField("name")
    private String name;

    @Schema(description = "活动地址")
    @TableField("address")
    private String address;

    @Schema(description = "纬度（地图选点）")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "经度（地图选点）")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "参考价格（线下签到支付，线上仅报名）")
    @TableField("price_from")
    private BigDecimal priceFrom;

    @Schema(description = "标签")
    @TableField("tag")
    private String tag;

    @Schema(description = "封面图")
    @TableField("cover_image")
    private String coverImage;

    @Schema(description = "活动介绍")
    @TableField("description")
    private String description;

    @Schema(description = "活动开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("start_time")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("end_time")
    private LocalDateTime endTime;

    @Schema(description = "报名上限，0表示不限")
    @TableField("enroll_limit")
    private Integer enrollLimit;

    @Schema(description = "已报名人数")
    @TableField("enroll_count")
    private Integer enrollCount;

    @Schema(description = "活动亮点 JSON 数组")
    @TableField("highlights")
    private String highlights;

    @Schema(description = "参与须知 JSON 数组")
    @TableField("notices")
    private String notices;

    @Schema(description = "状态：1上架 0下架")
    @TableField("status")
    private Integer status;

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

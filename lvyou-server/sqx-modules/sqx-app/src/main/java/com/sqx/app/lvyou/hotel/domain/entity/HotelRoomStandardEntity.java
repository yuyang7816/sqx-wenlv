package com.sqx.app.lvyou.hotel.domain.entity;

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
 * 酒店客房标准模板
 */
@Getter
@Setter
@TableName("tb_hotel_room_standard")
@Schema(name = "HotelRoomStandardEntity", description = "酒店客房标准模板")
public class HotelRoomStandardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("hotel_id")
    private Long hotelId;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("name")
    private String name;

    @TableField("floor_hint")
    private String floorHint;

    @TableField("facilities_json")
    private String facilitiesJson;

    @TableField("defaults_json")
    private String defaultsJson;

    @TableField("gallery_json")
    private String galleryJson;

    @TableField("tags_json")
    private String tagsJson;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

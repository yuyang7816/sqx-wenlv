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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 酒店房型
 */
@Getter
@Setter
@TableName("tb_hotel_room")
@Schema(name = "HotelRoomEntity", description = "酒店房型")
public class HotelRoomEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "酒店ID")
    @TableField("hotel_id")
    private Long hotelId;

    @Schema(description = "商户ID")
    @TableField("merchant_id")
    private Long merchantId;

    @Schema(description = "关联商户服务ID")
    @TableField("merchant_service_id")
    private Long merchantServiceId;

    @Schema(description = "房型名称")
    @TableField("name")
    private String name;

    @Schema(description = "规格描述")
    @TableField("spec")
    private String spec;

    @Schema(description = "每晚价格")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "库存")
    @TableField("stock")
    private Integer stock;

    @Schema(description = "封面图")
    @TableField("cover_image")
    private String coverImage;

    @Schema(description = "房型扩展 JSON")
    @TableField("room_detail_json")
    private String roomDetailJson;

    @Schema(description = "关联客房标准ID")
    @TableField("room_standard_id")
    private Long roomStandardId;

    @Schema(description = "状态：1在售 0下架")
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

package com.sqx.app.lvyou.order.domain.entity;

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
 * 订单明细
 */
@Getter
@Setter
@TableName("tb_order_item")
@Schema(name = "OrderItemEntity", description = "订单明细")
public class OrderItemEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID")
    @TableField("order_id")
    private Long orderId;

    @Schema(description = "订单号")
    @TableField("order_no")
    private String orderNo;

    @Schema(description = "明细类型：room/route/goods")
    @TableField("item_type")
    private String itemType;

    @Schema(description = "关联房型/线路/商品ID")
    @TableField("item_id")
    private Long itemId;

    @Schema(description = "明细名称")
    @TableField("item_name")
    private String itemName;

    @Schema(description = "规格描述")
    @TableField("item_spec")
    private String itemSpec;

    @Schema(description = "封面图")
    @TableField("cover_image")
    private String coverImage;

    @Schema(description = "数量")
    @TableField("quantity")
    private Integer quantity;

    @Schema(description = "单价")
    @TableField("unit_price")
    private BigDecimal unitPrice;

    @Schema(description = "小计")
    @TableField("total_price")
    private BigDecimal totalPrice;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

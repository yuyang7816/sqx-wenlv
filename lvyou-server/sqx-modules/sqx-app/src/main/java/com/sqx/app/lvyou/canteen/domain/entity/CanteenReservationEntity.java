package com.sqx.app.lvyou.canteen.domain.entity;

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
 * 餐饮预约
 */
@Getter
@Setter
@TableName("tb_canteen_reservation")
@Schema(name = "CanteenReservationEntity", description = "餐饮预约")
public class CanteenReservationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "预约业务单号")
    @TableField("reservation_no")
    private String reservationNo;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "商户ID")
    @TableField("merchant_id")
    private Long merchantId;

    @Schema(description = "关联商户订单ID")
    @TableField("order_id")
    private Long orderId;

    @Schema(description = "目的地ID")
    @TableField("destination_id")
    private Long destinationId;

    @Schema(description = "门店名称")
    @TableField("shop_name")
    private String shopName;

    @Schema(description = "预约菜品")
    @TableField("dish_name")
    private String dishName;

    @Schema(description = "联系人")
    @TableField("contact_name")
    private String contactName;

    @Schema(description = "联系电话")
    @TableField("contact_phone")
    private String contactPhone;

    @Schema(description = "用餐人数")
    @TableField("guest_count")
    private Integer guestCount;

    @Schema(description = "预约用餐时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("reserve_time")
    private LocalDateTime reserveTime;

    @Schema(description = "用餐地址")
    @TableField("address")
    private String address;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "状态：pending/confirmed/completed/cancelled")
    @TableField("status")
    private String status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

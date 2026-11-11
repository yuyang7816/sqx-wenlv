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
import java.time.LocalDateTime;

/**
 * 用户收货地址
 */
@Getter
@Setter
@TableName("tb_user_address")
@Schema(name = "UserAddressEntity", description = "用户收货地址")
public class UserAddressEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "收货人姓名")
    @TableField("receiver_name")
    private String receiverName;

    @Schema(description = "收货人电话")
    @TableField("receiver_phone")
    private String receiverPhone;

    @Schema(description = "省")
    @TableField("province")
    private String province;

    @Schema(description = "市")
    @TableField("city")
    private String city;

    @Schema(description = "区/县")
    @TableField("district")
    private String district;

    @Schema(description = "详细地址")
    @TableField("detail_address")
    private String detailAddress;

    @Schema(description = "是否默认：1是 0否")
    @TableField("is_default")
    private Integer isDefault;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

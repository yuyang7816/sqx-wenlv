package com.sqx.app.lvyou.route.domain.entity;

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
 * 旅游线路门店评价
 */
@Getter
@Setter
@TableName("tb_route_review")
@Schema(name = "RouteReviewEntity", description = "旅游线路门店评价")
public class RouteReviewEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "旅行社商户ID")
    @TableField("merchant_id")
    private Long merchantId;

    @Schema(description = "关联线路ID")
    @TableField("route_id")
    private Long routeId;

    @Schema(description = "关联订单ID")
    @TableField("order_id")
    private Long orderId;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "评价用户展示名")
    @TableField("user_name")
    private String userName;

    @Schema(description = "评价内容")
    @TableField("content")
    private String content;

    @Schema(description = "评分1-5")
    @TableField("rating")
    private Integer rating;

    @Schema(description = "评价图片URL列表(JSON)")
    @TableField("images")
    private String images;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

package com.sqx.app.lvyou.canteen.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 餐饮预约详情/列表项
 */
@Data
@Schema(description = "餐饮预约")
public class CanteenReservationVo {

    @Schema(description = "预约ID")
    private Long id;

    @Schema(description = "预约业务单号")
    private String reservationNo;

    @Schema(description = "门店名称")
    private String shopName;

    @Schema(description = "关联商户ID")
    private Long merchantId;

    @Schema(description = "门店封面图")
    private String shopCover;

    @Schema(description = "预约菜品")
    private String dishName;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "用餐人数")
    private Integer guestCount;

    @Schema(description = "预约时间 yyyy-MM-dd HH:mm")
    private String reserveTime;

    @Schema(description = "用餐地址")
    private String address;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态：pending/confirmed/completed/cancelled")
    private String status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "创建时间 yyyy-MM-dd HH:mm")
    private String createdAt;
}

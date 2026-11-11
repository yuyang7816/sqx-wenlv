package com.sqx.app.lvyou.merchant.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 商户资料
 */
@Getter
@Setter
@Schema(description = "商户资料")
public class MerchantProfileVo {

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户业务编码")
    private String merchantCode;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "门店类型")
    private String storeType;

    @Schema(description = "门店类型文案")
    private String typeLabel;

    @Schema(description = "门店地区")
    private String storeRegion;

    @Schema(description = "门店地址")
    private String storeAddress;

    @Schema(description = "营业时间")
    private String storeHours;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "统一社会信用代码")
    private String licenseNo;

    @Schema(description = "法人姓名")
    private String legalName;

    @Schema(description = "经营状态文案")
    private String statusLabel;

    @Schema(description = "是否营业中")
    private Boolean isOpen;

    @Schema(description = "保证金状态：paid/unpaid")
    private String depositStatus;

    @Schema(description = "是否可经营（已缴足保证金）")
    private Boolean canOperate;

    @Schema(description = "目的地名称")
    private String destinationName;

    @Schema(description = "审核通过时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    @Schema(description = "当前是否排队（仅餐饮）")
    private Boolean queueEnabled;

    @Schema(description = "前面排队桌数（仅餐饮）")
    private Integer queueTableCount;

    @Schema(description = "排队展示文案（仅餐饮，C端/B端预览）")
    private String queueDisplayText;
}

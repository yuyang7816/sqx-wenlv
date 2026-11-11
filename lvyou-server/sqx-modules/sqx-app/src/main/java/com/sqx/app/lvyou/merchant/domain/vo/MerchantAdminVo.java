package com.sqx.app.lvyou.merchant.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 管理端商户信息
 */
@Data
@Schema(description = "管理端商户信息")
public class MerchantAdminVo {

    @Schema(description = "商户ID")
    private Long id;

    @Schema(description = "商户业务编码")
    private String merchantCode;

    @Schema(description = "关联用户ID")
    private Long userId;

    @Schema(description = "用户手机号")
    private String userPhone;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "所属目的地ID")
    private Long destinationId;

    @Schema(description = "所属目的地名称")
    private String destinationName;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "门店类型")
    private String storeType;

    @Schema(description = "门店类型文案")
    private String storeTypeLabel;

    @Schema(description = "非遗好店分类编码")
    private String shopCategory;

    @Schema(description = "非遗好店分类名称")
    private String shopCategoryLabel;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "门店地区")
    private String storeRegion;

    @Schema(description = "门店详细地址")
    private String storeAddress;

    @Schema(description = "门店详细地址（不含地区）")
    private String storeAddressDetail;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "营业时间")
    private String storeHours;

    @Schema(description = "统一社会信用代码")
    private String licenseNo;

    @Schema(description = "营业执照图片URL")
    private String licenseImage;

    @Schema(description = "法人姓名")
    private String legalName;

    @Schema(description = "法人身份证号")
    private String legalIdNo;

    @Schema(description = "法人身份证正面URL")
    private String legalIdFront;

    @Schema(description = "法人身份证反面URL")
    private String legalIdBack;

    @Schema(description = "行业资质图片")
    private Map<String, String> qualImages;

    @Schema(description = "账号状态：1正常 2禁用")
    private Integer status;

    @Schema(description = "账号状态文案")
    private String statusLabel;

    @Schema(description = "营业状态：1营业中 0休息中")
    private Integer isOpen;

    @Schema(description = "营业状态文案")
    private String isOpenLabel;

    @Schema(description = "首页推荐：1开启 0关闭")
    private Integer homeRecommendEnabled;

    @Schema(description = "首页推荐文案")
    private String homeRecommendEnabledLabel;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "应缴保证金")
    private BigDecimal depositRequired;

    @Schema(description = "已缴保证金")
    private BigDecimal depositPaid;

    @Schema(description = "保证金状态：paid/unpaid")
    private String depositStatus;

    @Schema(description = "保证金状态文案")
    private String depositStatusLabel;

    @Schema(description = "未处理订单数")
    private Long unprocessedOrderCount;

    @Schema(description = "待审核退保单号")
    private String pendingDepositRefundNo;

    @Schema(description = "待审核退保金额")
    private BigDecimal pendingDepositRefundAmount;

    @Schema(description = "待审核退保状态：pending/approved/rejected")
    private String pendingDepositRefundStatus;
}

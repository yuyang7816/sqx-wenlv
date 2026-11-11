package com.sqx.app.lvyou.merchant.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户端商户入驻状态
 */
@Data
@Schema(description = "商户入驻状态")
public class MerchantStatusVo {

    @Schema(description = "入驻状态：none/pending/approved/rejected")
    private String status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "申请单号")
    private String applyNo;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "门店类型")
    private String storeType;

    @Schema(description = "门店类型文案")
    private String storeTypeLabel;

    @Schema(description = "审核备注（驳回原因）")
    private String auditRemark;

    @Schema(description = "商户业务编码")
    private String merchantCode;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditedAt;

    @Schema(description = "申请详情（含证照字段，便于结果页展示）")
    private MerchantApplyDetailVo applyDetail;

    /**
     * 申请详情快照
     */
    @Data
    @Schema(description = "入驻申请详情")
    public static class MerchantApplyDetailVo {

        @Schema(description = "联系人姓名")
        private String contactName;

        @Schema(description = "联系电话")
        private String contactPhone;

        @Schema(description = "门店地区")
        private String storeRegion;

        @Schema(description = "门店详细地址（不含地区前缀）")
        private String storeAddressDetail;

        @Schema(description = "门店完整地址")
        private String storeAddress;

        @Schema(description = "纬度")
        private BigDecimal latitude;

        @Schema(description = "经度")
        private BigDecimal longitude;

        @Schema(description = "营业时间")
        private String storeHours;

        @Schema(description = "非遗好店分类编码")
        private String shopCategory;

        @Schema(description = "非遗好店分类名称")
        private String shopCategoryLabel;

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
    }
}

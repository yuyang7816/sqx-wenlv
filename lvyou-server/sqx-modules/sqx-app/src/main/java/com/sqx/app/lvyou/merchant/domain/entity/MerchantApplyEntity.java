package com.sqx.app.lvyou.merchant.domain.entity;

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
 * 商户入驻申请
 */
@Getter
@Setter
@TableName("tb_merchant_apply")
@Schema(name = "MerchantApplyEntity", description = "商户入驻申请")
public class MerchantApplyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "申请单号")
    @TableField("apply_no")
    private String applyNo;

    @Schema(description = "申请人用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "意向目的地ID")
    @TableField("destination_id")
    private Long destinationId;

    @Schema(description = "门店名称")
    @TableField("store_name")
    private String storeName;

    @Schema(description = "门店类型")
    @TableField("store_type")
    private String storeType;

    @Schema(description = "非遗好店分类（文创商户必填）")
    @TableField("shop_category")
    private String shopCategory;

    @Schema(description = "关联非遗工艺ID（历史字段，新入驻不再使用）")
    @TableField("craft_id")
    private Long craftId;

    @Schema(description = "联系人姓名")
    @TableField("contact_name")
    private String contactName;

    @Schema(description = "联系电话")
    @TableField("contact_phone")
    private String contactPhone;

    @Schema(description = "门店地区")
    @TableField("store_region")
    private String storeRegion;

    @Schema(description = "门店详细地址")
    @TableField("store_address")
    private String storeAddress;

    @Schema(description = "纬度（地图选点）")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "经度（地图选点）")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "营业时间")
    @TableField("store_hours")
    private String storeHours;

    @Schema(description = "统一社会信用代码")
    @TableField("license_no")
    private String licenseNo;

    @Schema(description = "营业执照图片URL")
    @TableField("license_image")
    private String licenseImage;

    @Schema(description = "法人姓名")
    @TableField("legal_name")
    private String legalName;

    @Schema(description = "法人身份证号")
    @TableField("legal_id_no")
    private String legalIdNo;

    @Schema(description = "法人身份证正面URL")
    @TableField("legal_id_front")
    private String legalIdFront;

    @Schema(description = "法人身份证反面URL")
    @TableField("legal_id_back")
    private String legalIdBack;

    @Schema(description = "行业资质图片JSON")
    @TableField("qual_images")
    private String qualImages;

    @Schema(description = "审核状态：0待审核 1通过 2驳回")
    @TableField("audit_status")
    private Integer auditStatus;

    @Schema(description = "审核备注")
    @TableField("audit_remark")
    private String auditRemark;

    @Schema(description = "审核通过后关联商户ID")
    @TableField("merchant_id")
    private Long merchantId;

    @Schema(description = "提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("submitted_at")
    private LocalDateTime submittedAt;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("audited_at")
    private LocalDateTime auditedAt;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

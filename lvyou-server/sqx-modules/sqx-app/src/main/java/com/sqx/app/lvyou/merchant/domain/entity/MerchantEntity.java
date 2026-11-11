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
 * 商户
 */
@Getter
@Setter
@TableName("tb_merchant")
@Schema(name = "MerchantEntity", description = "商户")
public class MerchantEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "商户业务编码")
    @TableField("merchant_code")
    private String merchantCode;

    @Schema(description = "关联用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "所属目的地ID")
    @TableField("destination_id")
    private Long destinationId;

    @Schema(description = "门店名称")
    @TableField("store_name")
    private String storeName;

    @Schema(description = "门店类型")
    @TableField("store_type")
    private String storeType;

    @Schema(description = "非遗好店分类（文创商户）")
    @TableField("shop_category")
    private String shopCategory;

    @Schema(description = "关联非遗工艺ID（历史字段）")
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

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "经度")
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

    @Schema(description = "状态：1正常 2禁用 0待激活")
    @TableField("status")
    private Integer status;

    @Schema(description = "营业状态：1营业中 0休息中")
    @TableField("is_open")
    private Integer isOpen;

    @Schema(description = "是否排队：1是 0否（仅餐饮商户使用）")
    @TableField("queue_enabled")
    private Integer queueEnabled;

    @Schema(description = "前面排队桌数（仅餐饮且 queue_enabled=1 时有效）")
    @TableField("queue_table_count")
    private Integer queueTableCount;

    @Schema(description = "首页推荐：1开启 0关闭")
    @TableField("home_recommend_enabled")
    private Integer homeRecommendEnabled;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

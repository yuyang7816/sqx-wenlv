package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 用户端提交商户入驻申请
 */
@Data
@Schema(description = "商户入驻申请")
public class MerchantApplyBo {

    @Schema(description = "联系人姓名")
    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;

    @Schema(description = "联系电话")
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    @Schema(description = "门店名称")
    @NotBlank(message = "门店名称不能为空")
    private String storeName;

    @Schema(description = "门店地区")
    @NotBlank(message = "门店地区不能为空")
    private String storeRegion;

    @Schema(description = "门店详细地址")
    @NotBlank(message = "门店详细地址不能为空")
    private String storeAddressDetail;

    @Schema(description = "门店完整地址（地区+详细地址，可选）")
    private String storeAddress;

    @Schema(description = "营业时间，如 09:00-21:00")
    private String storeHours;

    @Schema(description = "门店类型：hotel/restaurant/travel/culture")
    @NotBlank(message = "门店类型不能为空")
    private String storeType;

    @Schema(description = "非遗好店分类编码（文创商户必填，对齐 tb_heritage_shop_category.code）")
    private String shopCategory;

    @Schema(description = "关联非遗工艺ID（已废弃，新入驻勿传）")
    private Long craftId;

    @Schema(description = "统一社会信用代码")
    @NotBlank(message = "统一社会信用代码不能为空")
    private String licenseNo;

    @Schema(description = "营业执照图片URL（file/upload 返回）")
    @NotBlank(message = "营业执照图片不能为空")
    private String licenseImage;

    @Schema(description = "法人姓名")
    @NotBlank(message = "法人姓名不能为空")
    private String legalName;

    @Schema(description = "法人身份证号")
    @NotBlank(message = "法人身份证号不能为空")
    private String legalIdNo;

    @Schema(description = "法人身份证正面URL")
    @NotBlank(message = "法人身份证正面不能为空")
    private String legalIdFront;

    @Schema(description = "法人身份证反面URL")
    @NotBlank(message = "法人身份证反面不能为空")
    private String legalIdBack;

    @Schema(description = "行业资质图片，key 为资质项标识")
    private Map<String, String> qualImages;

    @Schema(description = "纬度（地图选点）")
    @NotNull(message = "请在地图上选择门店位置")
    private BigDecimal latitude;

    @Schema(description = "经度（地图选点）")
    @NotNull(message = "请在地图上选择门店位置")
    private BigDecimal longitude;
}

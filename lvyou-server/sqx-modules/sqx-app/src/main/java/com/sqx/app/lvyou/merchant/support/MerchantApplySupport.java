package com.sqx.app.lvyou.merchant.support;

import com.sqx.app.lvyou.merchant.domain.bo.MerchantApplyBo;
import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * 商户入驻字段校验与地址组装（C 端与管理端共用）
 */
public final class MerchantApplySupport {

    private MerchantApplySupport() {
    }

    public static void validateApplyPayload(MerchantApplyBo applyBo) {
        if (applyBo == null) {
            throw new BizException("入驻资料不能为空");
        }
        if (!MerchantStoreType.isValid(applyBo.getStoreType())) {
            throw new BizException("门店类型不正确");
        }
        if (StringUtils.isBlank(StringUtils.trimToNull(applyBo.getContactName()))) {
            throw new BizException("请填写商户联系人");
        }
        if (StringUtils.isBlank(StringUtils.trimToNull(applyBo.getContactPhone()))) {
            throw new BizException("请填写联系方式");
        }
        if (StringUtils.isBlank(StringUtils.trimToNull(applyBo.getStoreName()))) {
            throw new BizException("请填写门店名称");
        }
        if (StringUtils.isBlank(StringUtils.trimToNull(applyBo.getStoreRegion()))) {
            throw new BizException("请选择所在地区");
        }
        if (StringUtils.isBlank(StringUtils.trimToNull(applyBo.getStoreAddressDetail()))) {
            throw new BizException("请填写详细地址");
        }
        if (StringUtils.isBlank(StringUtils.trimToNull(applyBo.getLicenseNo()))) {
            throw new BizException("请填写统一社会信用代码");
        }
        if (StringUtils.isBlank(StringUtils.trimToNull(applyBo.getLicenseImage()))) {
            throw new BizException("请上传营业执照");
        }
        if (StringUtils.isBlank(StringUtils.trimToNull(applyBo.getLegalName()))) {
            throw new BizException("请填写法人姓名");
        }
        if (StringUtils.isBlank(StringUtils.trimToNull(applyBo.getLegalIdNo()))) {
            throw new BizException("请填写法人身份证号");
        }
        if (StringUtils.isBlank(StringUtils.trimToNull(applyBo.getLegalIdFront()))) {
            throw new BizException("请上传身份证人像面");
        }
        if (StringUtils.isBlank(StringUtils.trimToNull(applyBo.getLegalIdBack()))) {
            throw new BizException("请上传身份证国徽面");
        }
        MerchantQualificationSupport.validateQualImages(applyBo.getStoreType(), applyBo.getQualImages());
        requireCoordinates(applyBo.getLatitude(), applyBo.getLongitude());
    }

    public static String resolveStoreAddress(MerchantApplyBo applyBo) {
        if (StringUtils.isNotBlank(applyBo.getStoreAddress())) {
            return StringUtils.trim(applyBo.getStoreAddress());
        }
        return StringUtils.trimToEmpty(applyBo.getStoreRegion()) + StringUtils.trimToEmpty(applyBo.getStoreAddressDetail());
    }

    public static void requireCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new BizException("请在地图上选择门店位置");
        }
    }
}

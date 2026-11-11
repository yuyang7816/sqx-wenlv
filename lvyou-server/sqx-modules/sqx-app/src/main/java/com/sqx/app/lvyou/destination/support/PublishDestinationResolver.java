package com.sqx.app.lvyou.destination.support;

import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.common.core.exception.BizException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 发布内容时自动解析目的地：优先经纬度，其次关联商户。
 */
@Component
public class PublishDestinationResolver {

    @Resource
    private DestinationSupport destinationSupport;

    @Resource
    private IMerchantService iMerchantService;

    public Long requireFromCoordinates(BigDecimal latitude, BigDecimal longitude) {
        return destinationSupport.resolvePublishDestination(latitude, longitude);
    }

    public Long tryFromCoordinates(BigDecimal latitude, BigDecimal longitude) {
        return destinationSupport.tryResolvePublishDestination(latitude, longitude);
    }

    public Long requireFromCoordinatesOrMerchant(BigDecimal latitude, BigDecimal longitude, Long merchantId) {
        Long fromCoordinates = tryFromCoordinates(latitude, longitude);
        if (fromCoordinates != null) {
            return fromCoordinates;
        }
        return requireFromMerchant(merchantId);
    }

    public Long requireFromMerchant(Long merchantId) {
        if (merchantId == null) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }
        MerchantEntity merchant = iMerchantService.getById(merchantId);
        if (merchant == null || merchant.getDestinationId() == null) {
            throw new BizException(AppError.DESTINATION_LOCATION_UNMATCHED);
        }
        return merchant.getDestinationId();
    }
}

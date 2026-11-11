package com.sqx.app.lvyou.merchant.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.common.core.exception.BizException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 商户鉴权辅助
 */
@Component
public class MerchantAuthSupport {

    private static final int MERCHANT_STATUS_NORMAL = 1;

    @Resource
    private IMerchantService iMerchantService;

    /**
     * 校验当前用户为已审核通过且正常状态的商户
     */
    public MerchantEntity requireApprovedMerchant(Long userId) {
        MerchantEntity merchant = findMerchantByUserId(userId);
        if (merchant == null) {
            throw new BizException(AppError.MERCHANT_NOT_APPROVED);
        }
        if (!Objects.equals(merchant.getStatus(), MERCHANT_STATUS_NORMAL)) {
            throw new BizException(AppError.MERCHANT_DISABLED);
        }
        return merchant;
    }

    public MerchantEntity findMerchantByUserId(Long userId) {
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantEntity::getUserId, userId)
                .orderByDesc(MerchantEntity::getId)
                .last("LIMIT 1");
        return iMerchantService.getOne(queryWrapper, false);
    }
}

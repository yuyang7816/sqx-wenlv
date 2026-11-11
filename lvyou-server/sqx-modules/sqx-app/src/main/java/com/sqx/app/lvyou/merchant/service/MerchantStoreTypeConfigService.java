package com.sqx.app.lvyou.merchant.service;

import com.sqx.app.lvyou.merchant.domain.bo.MerchantStoreTypeSaveBo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantStoreTypeVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商户入驻门店类型配置（sys_dict lvyou_merchant_type）
 */
public interface MerchantStoreTypeConfigService {

    List<MerchantStoreTypeVo> listAll();

    List<MerchantStoreTypeVo> listActive();

    boolean isValid(String code);

    String labelOf(String code);

    BigDecimal resolveDeposit(String code);

    BigDecimal parseDepositAmount(String rawValue, String code);

    void save(MerchantStoreTypeSaveBo saveBo);

    void update(MerchantStoreTypeSaveBo saveBo);

    void delete(Long id);
}

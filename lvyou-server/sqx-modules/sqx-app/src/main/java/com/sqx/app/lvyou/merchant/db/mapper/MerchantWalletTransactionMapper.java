package com.sqx.app.lvyou.merchant.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantWalletTransactionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户钱包流水 Mapper
 */
@Mapper
public interface MerchantWalletTransactionMapper extends BaseMapper<MerchantWalletTransactionEntity> {
}

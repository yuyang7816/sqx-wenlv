package com.sqx.app.lvyou.wallet.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.lvyou.wallet.domain.entity.UserWalletTransactionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户钱包流水 Mapper
 */
@Mapper
public interface UserWalletTransactionMapper extends BaseMapper<UserWalletTransactionEntity> {
}

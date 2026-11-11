package com.sqx.app.lvyou.wallet.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.lvyou.wallet.domain.entity.UserWalletEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户钱包 Mapper
 */
@Mapper
public interface UserWalletMapper extends BaseMapper<UserWalletEntity> {
}

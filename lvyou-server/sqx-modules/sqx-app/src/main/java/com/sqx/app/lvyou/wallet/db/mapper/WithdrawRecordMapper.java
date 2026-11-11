package com.sqx.app.lvyou.wallet.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.lvyou.wallet.domain.entity.WithdrawRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提现记录 Mapper
 */
@Mapper
public interface WithdrawRecordMapper extends BaseMapper<WithdrawRecordEntity> {
}

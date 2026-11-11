package com.sqx.app.lvyou.order.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.lvyou.order.domain.entity.UserAddressEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收货地址 Mapper
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddressEntity> {
}

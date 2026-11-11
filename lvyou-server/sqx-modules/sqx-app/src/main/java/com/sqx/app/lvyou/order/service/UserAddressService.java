package com.sqx.app.lvyou.order.service;

import com.sqx.app.lvyou.order.domain.bo.AddressDeleteBo;
import com.sqx.app.lvyou.order.domain.bo.AddressSaveBo;
import com.sqx.app.lvyou.order.domain.vo.AddressVo;

import java.util.List;

/**
 * 用户收货地址业务服务
 */
public interface UserAddressService {

    List<AddressVo> listByUser(Long userId);

    Long save(Long userId, AddressSaveBo saveBo);

    void delete(Long userId, AddressDeleteBo deleteBo);
}

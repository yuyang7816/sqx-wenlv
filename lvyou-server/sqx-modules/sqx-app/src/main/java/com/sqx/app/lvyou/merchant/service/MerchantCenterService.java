package com.sqx.app.lvyou.merchant.service;

import com.sqx.app.lvyou.hotel.domain.bo.HotelRoomStandardSaveBo;
import com.sqx.app.lvyou.hotel.domain.vo.HotelRoomStandardVo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantProfileUpdateBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantQueueUpdateBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantServicePublishBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantServiceSaveBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantStoreDecorSaveBo;
import com.sqx.app.lvyou.home.domain.vo.HomeModuleAppVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDashboardVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantProfileVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantServiceVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantStoreDecorVo;

import java.util.List;

/**
 * 商户中心业务服务
 */
public interface MerchantCenterService {

    MerchantDashboardVo getDashboard(Long userId);

    Long getPendingOrderCount(Long userId);

    MerchantProfileVo getProfile(Long userId);

    MerchantProfileVo updateProfile(Long userId, MerchantProfileUpdateBo updateBo);

    MerchantProfileVo updateQueueStatus(Long userId, MerchantQueueUpdateBo updateBo);

    List<MerchantServiceVo> listServices(Long userId, String tab);

    MerchantServiceVo getService(Long userId, Long serviceId);

    MerchantServiceVo saveService(Long userId, MerchantServiceSaveBo saveBo);

    void publishService(Long userId, Long serviceId, MerchantServicePublishBo publishBo);

    void deleteService(Long userId, Long serviceId);

    List<HomeModuleAppVo> listHomeTabOptions(Long userId);

    MerchantStoreDecorVo getStoreDecor(Long userId);

    MerchantStoreDecorVo saveStoreDecor(Long userId, MerchantStoreDecorSaveBo saveBo);

    List<HotelRoomStandardVo> listRoomStandards(Long userId);

    HotelRoomStandardVo getRoomStandard(Long userId, Long standardId);

    HotelRoomStandardVo saveRoomStandard(Long userId, HotelRoomStandardSaveBo saveBo);

    void deleteRoomStandard(Long userId, Long standardId);
}

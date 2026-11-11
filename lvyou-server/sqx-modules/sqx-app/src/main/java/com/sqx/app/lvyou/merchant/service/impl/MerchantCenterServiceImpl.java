package com.sqx.app.lvyou.merchant.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.destination.support.DestinationDisplaySupport;
import com.sqx.app.lvyou.hotel.db.service.IHotelRoomService;
import com.sqx.app.lvyou.hotel.db.service.IHotelRoomStandardService;
import com.sqx.app.lvyou.hotel.db.service.IHotelService;
import com.sqx.app.lvyou.hotel.domain.bo.HotelRoomStandardSaveBo;
import com.sqx.app.lvyou.hotel.domain.entity.HotelEntity;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomEntity;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomStandardEntity;
import com.sqx.app.lvyou.hotel.domain.vo.HotelPolicyVo;
import com.sqx.app.lvyou.hotel.domain.vo.HotelRoomStandardVo;
import com.sqx.app.lvyou.hotel.support.HotelInventorySupport;
import com.sqx.app.lvyou.hotel.util.HotelPolicyUtil;
import com.sqx.app.lvyou.hotel.util.HotelRoomDetailUtil;
import com.sqx.app.lvyou.hotel.util.HotelRoomStandardUtil;
import com.sqx.app.lvyou.home.domain.vo.HomeModuleAppVo;
import com.sqx.app.lvyou.home.service.HomeModuleService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantApplyService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantServiceItemService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantStoreDecorService;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantProfileUpdateBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantQueueUpdateBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantServicePublishBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantServiceSaveBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantStoreDecorSaveBo;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantApplyEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantServiceEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantStoreDecorEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantWalletEntity;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDashboardVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantProfileVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantServiceVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantStoreDecorVo;
import com.sqx.app.lvyou.merchant.enums.MerchantApplyAuditStatus;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.merchant.enums.MerchantServiceStatus;
import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import com.sqx.app.lvyou.merchant.service.MerchantCenterService;
import com.sqx.app.lvyou.merchant.service.MerchantContentPublishService;
import com.sqx.app.lvyou.merchant.service.MerchantOrderService;
import com.sqx.app.lvyou.merchant.service.MerchantWalletService;
import com.sqx.app.lvyou.merchant.support.MerchantAuthSupport;
import com.sqx.app.lvyou.merchant.support.MerchantHomeTabSupport;
import com.sqx.app.lvyou.merchant.support.RestaurantQueueSupport;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 商户中心业务服务实现
 */
@Service
public class MerchantCenterServiceImpl implements MerchantCenterService {

    private static final int MERCHANT_OPEN = 1;
    private static final int MERCHANT_CLOSED = 0;
    private static final DateTimeFormatter SERVICE_CODE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Set<String> LEGACY_DECOR_SERVICE_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "linkedServices", "roomTypes", "serviceIds", "linkedRoomIds"
    )));

    @Resource
    private MerchantAuthSupport merchantAuthSupport;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private IMerchantApplyService iMerchantApplyService;

    @Resource
    private IMerchantServiceItemService iMerchantServiceItemService;

    @Resource
    private HomeModuleService homeModuleService;

    @Resource
    private IMerchantStoreDecorService iMerchantStoreDecorService;

    @Resource
    private IOrderService iOrderService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private MerchantOrderService merchantOrderService;

    @Resource
    private MerchantWalletService merchantWalletService;

    @Resource
    private MerchantContentPublishService merchantContentPublishService;

    @Resource
    private IHotelService iHotelService;

    @Resource
    private IHotelRoomStandardService iHotelRoomStandardService;

    @Resource
    private IHotelRoomService iHotelRoomService;

    @Resource
    private HotelInventorySupport hotelInventorySupport;

    @Override
    public MerchantDashboardVo getDashboard(Long userId) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        ensureClosedUntilDepositPaid(merchant);
        MerchantWalletEntity wallet = merchantWalletService.ensureWallet(merchant.getId());
        String storeType = MerchantStoreType.normalize(merchant.getStoreType());
        boolean isRestaurant = MerchantStoreType.RESTAURANT.getCode().equals(storeType)
                || MerchantStoreType.isLegacyGoose(storeType);

        MerchantDashboardVo vo = new MerchantDashboardVo();
        vo.setMerchant(buildProfileVo(merchant));
        vo.setService(buildServiceStats(merchant.getId(), isRestaurant));
        vo.setOrder(buildOrderStats(merchant.getId()));
        vo.setRevenue(buildRevenueStats(merchant.getId(), wallet, isRestaurant));
        vo.setWallet(buildWalletSummary(wallet));
        if (merchantWalletService.isDepositPaid(merchant.getId())) {
            vo.setPendingCount(vo.getOrder().getPending());
            if (MerchantStoreType.HOTEL.getCode().equals(storeType)) {
                vo.getOrder().setPendingVerify(merchantOrderService.countPendingVerifyOrders(merchant.getId()));
                vo.setPendingVerifyOrders(merchantOrderService.listPendingVerifyOrders(merchant.getId(), 5));
            } else if (MerchantStoreType.TRAVEL.getCode().equals(storeType)) {
                vo.getOrder().setPendingVerify(merchantOrderService.countPendingVerifyOrders(merchant.getId()));
                vo.setPendingVerifyOrders(merchantOrderService.listPendingVerifyOrders(merchant.getId(), 5));
            } else if (MerchantStoreType.CULTURE.getCode().equals(storeType)) {
                vo.getOrder().setPendingShip(merchantOrderService.countPendingShipOrders(merchant.getId()));
                vo.setPendingShipOrders(merchantOrderService.listPendingShipOrders(merchant.getId(), 5));
            }
        } else {
            vo.setPendingCount(0L);
            if (vo.getOrder() != null) {
                vo.getOrder().setPendingVerify(0L);
                vo.getOrder().setPendingShip(0L);
                vo.getOrder().setPendingRouteBooking(0L);
            }
            vo.setPendingVerifyOrders(Collections.emptyList());
            vo.setPendingShipOrders(Collections.emptyList());
            vo.setPendingRouteBookings(Collections.emptyList());
        }
        return vo;
    }

    @Override
    public Long getPendingOrderCount(Long userId) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        if (!merchantWalletService.isDepositPaid(merchant.getId())) {
            return 0L;
        }
        return merchantOrderService.countPendingOrders(merchant.getId());
    }

    @Override
    public MerchantProfileVo getProfile(Long userId) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        ensureClosedUntilDepositPaid(merchant);
        return buildProfileVo(merchant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantProfileVo updateProfile(Long userId, MerchantProfileUpdateBo updateBo) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        assertMerchantOperationAllowed(merchant);
        LambdaUpdateWrapper<MerchantEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantEntity::getId, merchant.getId());
        boolean changed = false;
        boolean contentSyncNeeded = false;

        if (StringUtils.isNotBlank(updateBo.getContactName())) {
            updateWrapper.set(MerchantEntity::getContactName, StringUtils.trim(updateBo.getContactName()));
            changed = true;
        }
        if (StringUtils.isNotBlank(updateBo.getContactPhone())) {
            updateWrapper.set(MerchantEntity::getContactPhone, StringUtils.trim(updateBo.getContactPhone()));
            changed = true;
            contentSyncNeeded = true;
        }
        if (StringUtils.isNotBlank(updateBo.getStoreAddress())) {
            updateWrapper.set(MerchantEntity::getStoreAddress, StringUtils.trim(updateBo.getStoreAddress()));
            changed = true;
            contentSyncNeeded = true;
        }
        if (StringUtils.isNotBlank(updateBo.getStoreHours())) {
            updateWrapper.set(MerchantEntity::getStoreHours, StringUtils.trim(updateBo.getStoreHours()));
            changed = true;
            contentSyncNeeded = true;
        }
        if (updateBo.getIsOpen() != null) {
            updateWrapper.set(MerchantEntity::getIsOpen, Boolean.TRUE.equals(updateBo.getIsOpen()) ? MERCHANT_OPEN : MERCHANT_CLOSED);
            changed = true;
            if (!Boolean.TRUE.equals(updateBo.getIsOpen()) && RestaurantQueueSupport.isRestaurant(merchant)) {
                updateWrapper.set(MerchantEntity::getQueueEnabled, RestaurantQueueSupport.QUEUE_DISABLED);
                updateWrapper.set(MerchantEntity::getQueueTableCount, 0);
            }
        }

        if (changed) {
            iMerchantService.update(new MerchantEntity(), updateWrapper);
            merchant = iMerchantService.getById(merchant.getId());
            if (contentSyncNeeded) {
                merchantContentPublishService.syncStoreDecor(merchant, loadDecorMap(merchant.getId()));
            }
        }
        return buildProfileVo(merchant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantProfileVo updateQueueStatus(Long userId, MerchantQueueUpdateBo updateBo) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        assertMerchantOperationAllowed(merchant);
        RestaurantQueueSupport.validateUpdate(merchant, updateBo.getQueueEnabled(), updateBo.getQueueTableCount());
        RestaurantQueueSupport.applyQueueFields(merchant, updateBo.getQueueEnabled(), updateBo.getQueueTableCount());
        iMerchantService.updateById(merchant);
        merchant = iMerchantService.getById(merchant.getId());
        return buildProfileVo(merchant);
    }

    @Override
    public List<MerchantServiceVo> listServices(Long userId, String tab) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        LambdaQueryWrapper<MerchantServiceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantServiceEntity::getMerchantId, merchant.getId())
                .orderByDesc(MerchantServiceEntity::getUpdateTime)
                .orderByDesc(MerchantServiceEntity::getId);

        String normalizedTab = StringUtils.defaultIfBlank(tab, "all");
        if (!"all".equalsIgnoreCase(normalizedTab)) {
            queryWrapper.eq(MerchantServiceEntity::getStatus, MerchantServiceStatus.normalize(normalizedTab));
        }

        List<MerchantServiceEntity> entities = iMerchantServiceItemService.list(queryWrapper);
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }
        return entities.stream().map(entity -> {
            MerchantServiceVo vo = toServiceVo(entity);
            enrichHotelAvailableStock(vo, entity);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public MerchantServiceVo getService(Long userId, Long serviceId) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        MerchantServiceEntity entity = requireOwnedService(merchant.getId(), serviceId);
        return toServiceVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantServiceVo saveService(Long userId, MerchantServiceSaveBo saveBo) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        assertMerchantOperationAllowed(merchant);
        String normalizedStatus = MerchantServiceStatus.normalize(saveBo.getStatus());
        String homeTabKey = StringUtils.trimToNull(saveBo.getHomeTabKey());
        if (StringUtils.isBlank(homeTabKey)) {
            homeTabKey = MerchantHomeTabSupport.resolveTabKey(merchant.getStoreType());
        }
        validateHomeTabKey(normalizedStatus, homeTabKey);

        MerchantServiceEntity entity;
        if (saveBo.getId() != null) {
            entity = requireOwnedService(merchant.getId(), saveBo.getId());
            entity.setName(StringUtils.trim(saveBo.getName()));
            entity.setPrice(defaultDecimal(saveBo.getPrice()));
            entity.setStock(defaultInteger(saveBo.getStock()));
            entity.setDescription(StringUtils.trimToNull(saveBo.getDesc()));
            entity.setSpec(StringUtils.trimToNull(saveBo.getSpec()));
            entity.setMeetPoint(StringUtils.trimToNull(saveBo.getMeetPoint()));
            entity.setSpots(StringUtils.trimToNull(saveBo.getSpots()));
            entity.setTravelCity(StringUtils.trimToNull(saveBo.getTravelCity()));
            entity.setDayCount(normalizeDayCount(saveBo.getDayCount(), saveBo.getSpec()));
            entity.setItineraryJson(serializeItineraryDays(saveBo.getItineraryDays()));
            entity.setGoodsSpecs(StringUtils.trimToNull(saveBo.getGoodsSpecs()));
            entity.setDetail(StringUtils.trimToNull(saveBo.getDetail()));
            applyRoomDetailFields(entity, merchant, saveBo);
            entity.setMonthlySales(defaultInteger(saveBo.getMonthlySales()));
            entity.setCoverImage(StringUtils.trimToNull(saveBo.getCover()));
            entity.setBannerImages(serializeBannerImages(saveBo.getBanners()));
            entity.setDetailImages(serializeBannerImages(saveBo.getDetailImages()));
            entity.setHomeTabKey(homeTabKey);
            if (StringUtils.isNotBlank(saveBo.getStatus())) {
                entity.setStatus(normalizedStatus);
            }
            iMerchantServiceItemService.updateById(entity);
        } else {
            entity = new MerchantServiceEntity();
            entity.setMerchantId(merchant.getId());
            entity.setServiceCode(generateServiceCode());
            entity.setName(StringUtils.trim(saveBo.getName()));
            entity.setPrice(defaultDecimal(saveBo.getPrice()));
            entity.setStock(defaultInteger(saveBo.getStock()));
            entity.setDescription(StringUtils.trimToNull(saveBo.getDesc()));
            entity.setSpec(StringUtils.trimToNull(saveBo.getSpec()));
            entity.setMeetPoint(StringUtils.trimToNull(saveBo.getMeetPoint()));
            entity.setSpots(StringUtils.trimToNull(saveBo.getSpots()));
            entity.setTravelCity(StringUtils.trimToNull(saveBo.getTravelCity()));
            entity.setDayCount(normalizeDayCount(saveBo.getDayCount(), saveBo.getSpec()));
            entity.setItineraryJson(serializeItineraryDays(saveBo.getItineraryDays()));
            entity.setGoodsSpecs(StringUtils.trimToNull(saveBo.getGoodsSpecs()));
            entity.setDetail(StringUtils.trimToNull(saveBo.getDetail()));
            applyRoomDetailFields(entity, merchant, saveBo);
            entity.setMonthlySales(defaultInteger(saveBo.getMonthlySales()));
            entity.setCoverImage(StringUtils.trimToNull(saveBo.getCover()));
            entity.setBannerImages(serializeBannerImages(saveBo.getBanners()));
            entity.setDetailImages(serializeBannerImages(saveBo.getDetailImages()));
            entity.setHomeTabKey(homeTabKey);
            entity.setStatus(normalizedStatus);
            entity.setSort(0);
            iMerchantServiceItemService.save(entity);
        }
        merchantContentPublishService.syncService(merchant, entity);
        return toServiceVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishService(Long userId, Long serviceId, MerchantServicePublishBo publishBo) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        assertMerchantOperationAllowed(merchant);
        MerchantServiceEntity entity = requireOwnedService(merchant.getId(), serviceId);
        String targetStatus = MerchantServiceStatus.normalize(publishBo.getStatus());
        if (!MerchantServiceStatus.ON_SALE.getCode().equals(targetStatus)
                && !MerchantServiceStatus.OFF_SALE.getCode().equals(targetStatus)) {
            throw new BizException(AppError.MERCHANT_SERVICE_STATUS_INVALID);
        }
        if (MerchantServiceStatus.ON_SALE.getCode().equals(targetStatus)) {
            validateHomeTabKey(targetStatus, entity.getHomeTabKey());
        }
        entity.setStatus(targetStatus);
        iMerchantServiceItemService.updateById(entity);
        merchantContentPublishService.syncService(merchant, entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteService(Long userId, Long serviceId) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        assertMerchantOperationAllowed(merchant);
        MerchantServiceEntity entity = requireOwnedService(merchant.getId(), serviceId);
        if (MerchantServiceStatus.ON_SALE.getCode().equals(entity.getStatus())) {
            throw new BizException(AppError.MERCHANT_SERVICE_DELETE_ON_SALE);
        }
        merchantContentPublishService.removeService(merchant, serviceId);
        iMerchantServiceItemService.removeById(serviceId);
    }

    @Override
    public List<HomeModuleAppVo> listHomeTabOptions(Long userId) {
        merchantAuthSupport.requireApprovedMerchant(userId);
        return homeModuleService.listSelectableTabs();
    }

    @Override
    public MerchantStoreDecorVo getStoreDecor(Long userId) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        return buildStoreDecorVo(merchant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantStoreDecorVo saveStoreDecor(Long userId, MerchantStoreDecorSaveBo saveBo) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        Map<String, Object> currentDecor = loadDecorMap(merchant.getId());
        Map<String, Object> incoming = saveBo.getDecor() == null ? Collections.emptyMap() : saveBo.getDecor();
        Map<String, Object> merged = mergeDecorMaps(currentDecor, incoming);
        stripLegacyDecorServiceKeys(merged);
        merged.put("updatedAt", System.currentTimeMillis());

        MerchantStoreDecorEntity decorEntity = findDecorEntity(merchant.getId());
        if (decorEntity == null) {
            decorEntity = new MerchantStoreDecorEntity();
            decorEntity.setMerchantId(merchant.getId());
            decorEntity.setDecorJson(JSON.toJSONString(merged));
            iMerchantStoreDecorService.save(decorEntity);
        } else {
            decorEntity.setDecorJson(JSON.toJSONString(merged));
            iMerchantStoreDecorService.updateById(decorEntity);
        }
        merchantContentPublishService.syncStoreDecor(merchant, merged);
        return buildStoreDecorVo(merchant);
    }

    @Override
    public List<HotelRoomStandardVo> listRoomStandards(Long userId) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        requireHotelMerchant(merchant);
        HotelEntity hotel = requireHotel(merchant.getId());
        LambdaQueryWrapper<HotelRoomStandardEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelRoomStandardEntity::getHotelId, hotel.getId())
                .orderByAsc(HotelRoomStandardEntity::getSort)
                .orderByDesc(HotelRoomStandardEntity::getId);
        List<HotelRoomStandardEntity> entities = iHotelRoomStandardService.list(queryWrapper);
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toRoomStandardVo).collect(Collectors.toList());
    }

    @Override
    public HotelRoomStandardVo getRoomStandard(Long userId, Long standardId) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        requireHotelMerchant(merchant);
        HotelRoomStandardEntity entity = requireOwnedRoomStandard(merchant, standardId);
        return toRoomStandardVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HotelRoomStandardVo saveRoomStandard(Long userId, HotelRoomStandardSaveBo saveBo) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        assertMerchantOperationAllowed(merchant);
        requireHotelMerchant(merchant);
        HotelEntity hotel = requireHotel(merchant.getId());
        HotelRoomStandardEntity entity;
        if (saveBo.getId() != null) {
            entity = requireOwnedRoomStandard(merchant, saveBo.getId());
        } else {
            entity = new HotelRoomStandardEntity();
            entity.setHotelId(hotel.getId());
            entity.setMerchantId(merchant.getId());
            entity.setSort(saveBo.getSort() == null ? 0 : saveBo.getSort());
            entity.setStatus(saveBo.getStatus() == null ? 1 : saveBo.getStatus());
        }
        entity.setName(StringUtils.trim(saveBo.getName()));
        entity.setFloorHint(StringUtils.trimToNull(saveBo.getFloorHint()));
        entity.setFacilitiesJson(HotelRoomStandardUtil.facilitiesToJson(saveBo.getFacilities()));
        entity.setDefaultsJson(HotelRoomStandardUtil.defaultsToJson(saveBo.getDefaults()));
        entity.setGalleryJson(HotelRoomStandardUtil.stringListToJson(saveBo.getGallery()));
        entity.setTagsJson(HotelRoomStandardUtil.stringListToJson(saveBo.getTags()));
        if (saveBo.getSort() != null) {
            entity.setSort(saveBo.getSort());
        }
        if (saveBo.getStatus() != null) {
            entity.setStatus(saveBo.getStatus());
        }
        if (entity.getId() == null) {
            iHotelRoomStandardService.save(entity);
        } else {
            iHotelRoomStandardService.updateById(entity);
        }
        syncLinkedRoomStandardServices(merchant, entity.getId());
        return toRoomStandardVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoomStandard(Long userId, Long standardId) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        assertMerchantOperationAllowed(merchant);
        requireHotelMerchant(merchant);
        requireOwnedRoomStandard(merchant, standardId);
        if (countLinkedRoomStandards(standardId) > 0) {
            throw new BizException(AppError.HOTEL_ROOM_STANDARD_IN_USE);
        }
        iHotelRoomStandardService.removeById(standardId);
    }

    private MerchantDashboardVo.ServiceStats buildServiceStats(Long merchantId, boolean isRestaurant) {
        MerchantDashboardVo.ServiceStats stats = new MerchantDashboardVo.ServiceStats();
        stats.setTotal(countServices(merchantId, null));
        stats.setOnSale(countServices(merchantId, MerchantServiceStatus.ON_SALE.getCode()));
        stats.setOnSaleLabel(isRestaurant ? "开放预约" : "上架中");
        return stats;
    }

    private MerchantDashboardVo.OrderStats buildOrderStats(Long merchantId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        MerchantDashboardVo.OrderStats stats = new MerchantDashboardVo.OrderStats();
        stats.setTotal(countOrders(merchantId, null, null, null));
        stats.setPending(merchantOrderService.countPendingOrders(merchantId));
        stats.setProcessing(countOrders(merchantId, MerchantOrderStatusGroup.PROCESSING, null, null));
        stats.setClosed(countOrders(merchantId, MerchantOrderStatusGroup.CLOSED, null, null));
        stats.setTodayCount(countOrders(merchantId, null, startOfDay, endOfDay));
        return stats;
    }

    private MerchantDashboardVo.RevenueStats buildRevenueStats(Long merchantId,
                                                               MerchantWalletEntity wallet,
                                                               boolean isRestaurant) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        MerchantDashboardVo.RevenueStats stats = new MerchantDashboardVo.RevenueStats();
        if (isRestaurant) {
            stats.setToday(BigDecimal.valueOf(merchantOrderService.countPaidOrders(merchantId, startOfDay, endOfDay)));
            stats.setMonth(BigDecimal.valueOf(merchantOrderService.countPaidOrders(merchantId, startOfMonth, endOfDay)));
            stats.setTotal(BigDecimal.valueOf(merchantOrderService.countPaidOrders(merchantId, null, null)));
        } else {
            stats.setToday(merchantOrderService.sumPaidAmount(merchantId, startOfDay, endOfDay));
            stats.setMonth(merchantOrderService.sumPaidAmount(merchantId, startOfMonth, endOfDay));
            stats.setTotal(merchantOrderService.sumPaidAmount(merchantId, null, null));
        }
        stats.setIsMoney(!isRestaurant);
        return stats;
    }

    private MerchantDashboardVo.WalletSummary buildWalletSummary(MerchantWalletEntity wallet) {
        MerchantDashboardVo.WalletSummary summary = new MerchantDashboardVo.WalletSummary();
        summary.setBalance(defaultDecimal(wallet.getBalance()));
        summary.setPendingSettle(defaultDecimal(wallet.getPendingSettle()));
        summary.setTotalIncome(merchantOrderService.sumPaidAmount(wallet.getMerchantId(), null, null));
        summary.setDepositRequired(defaultDecimal(wallet.getDepositRequired()));
        summary.setDepositPaid(defaultDecimal(wallet.getDepositPaid()));
        summary.setDepositStatus(resolveDepositStatus(wallet));
        return summary;
    }

    private void assertMerchantOperationAllowed(MerchantEntity merchant) {
        merchantWalletService.assertDepositPaid(merchant.getId());
    }

    private void ensureClosedUntilDepositPaid(MerchantEntity merchant) {
        if (merchant == null || merchant.getId() == null) {
            return;
        }
        if (merchantWalletService.isDepositPaid(merchant.getId())) {
            return;
        }
        if (!Objects.equals(merchant.getIsOpen(), MERCHANT_OPEN)) {
            return;
        }
        LambdaUpdateWrapper<MerchantEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantEntity::getId, merchant.getId())
                .set(MerchantEntity::getIsOpen, MERCHANT_CLOSED);
        iMerchantService.update(new MerchantEntity(), updateWrapper);
        merchant.setIsOpen(MERCHANT_CLOSED);
    }

    private String resolveDepositStatus(MerchantWalletEntity wallet) {
        if (wallet == null) {
            return "unpaid";
        }
        BigDecimal depositRequired = defaultDecimal(wallet.getDepositRequired());
        BigDecimal depositPaid = defaultDecimal(wallet.getDepositPaid());
        return depositPaid.compareTo(depositRequired) >= 0 ? "paid" : "unpaid";
    }

    private MerchantProfileVo buildProfileVo(MerchantEntity merchant) {
        MerchantProfileVo vo = new MerchantProfileVo();
        vo.setMerchantId(merchant.getId());
        vo.setMerchantCode(merchant.getMerchantCode());
        vo.setStoreName(merchant.getStoreName());
        vo.setStoreType(MerchantStoreType.normalize(merchant.getStoreType()));
        vo.setTypeLabel(MerchantStoreType.labelOf(merchant.getStoreType()));
        vo.setStoreRegion(merchant.getStoreRegion());
        vo.setStoreAddress(merchant.getStoreAddress());
        vo.setStoreHours(merchant.getStoreHours());
        vo.setContactName(merchant.getContactName());
        vo.setContactPhone(merchant.getContactPhone());
        vo.setLicenseNo(merchant.getLicenseNo());
        vo.setLegalName(merchant.getLegalName());
        boolean depositPaid = merchantWalletService.isDepositPaid(merchant.getId());
        vo.setDepositStatus(depositPaid ? "paid" : "unpaid");
        vo.setCanOperate(Boolean.valueOf(depositPaid));
        boolean open = depositPaid
                && (merchant.getIsOpen() == null || Objects.equals(merchant.getIsOpen(), MERCHANT_OPEN));
        vo.setIsOpen(open);
        vo.setStatusLabel(depositPaid ? (open ? "营业中" : "休息中") : "未营业");
        if (RestaurantQueueSupport.isRestaurant(merchant)) {
            boolean queueEnabled = Objects.equals(merchant.getQueueEnabled(), RestaurantQueueSupport.QUEUE_ENABLED);
            int tableCount = merchant.getQueueTableCount() == null ? 0 : merchant.getQueueTableCount();
            vo.setQueueEnabled(queueEnabled);
            vo.setQueueTableCount(queueEnabled ? tableCount : 0);
            vo.setQueueDisplayText(RestaurantQueueSupport.buildDisplayText(
                    merchant.getIsOpen(),
                    merchant.getQueueEnabled(),
                    merchant.getQueueTableCount()));
        }

        if (merchant.getDestinationId() != null) {
            DestinationEntity destination = iDestinationService.getById(merchant.getDestinationId());
            vo.setDestinationName(DestinationDisplaySupport.resolveLabel(
                    destination, merchant.getStoreAddress(), merchant.getStoreRegion()));
        } else {
            vo.setDestinationName(DestinationDisplaySupport.resolveLabel(
                    (String) null, merchant.getStoreAddress(), merchant.getStoreRegion()));
        }

        MerchantApplyEntity apply = findApprovedApply(merchant.getId());
        if (apply != null) {
            vo.setApprovedAt(apply.getAuditedAt());
        }
        return vo;
    }

    private MerchantStoreDecorVo buildStoreDecorVo(MerchantEntity merchant) {
        String typeKey = MerchantStoreType.normalize(merchant.getStoreType());
        MerchantProfileVo profile = buildProfileVo(merchant);
        Map<String, Object> storedDecor = loadDecorMap(merchant.getId());
        Map<String, Object> defaults = buildDefaultDecor(typeKey, profile);
        Map<String, Object> decor = mergeDecorMaps(defaults, storedDecor);
        stripLegacyDecorServiceKeys(decor);
        if ("hotel".equals(typeKey)) {
            enrichHotelDecorFromPublished(merchant.getId(), decor);
        }

        MerchantStoreDecorVo vo = new MerchantStoreDecorVo();
        vo.setTypeKey(typeKey);
        vo.setTypeMeta(buildStoreTypeMeta(typeKey));
        vo.setProfile(profile);
        vo.setDecor(decor);
        Long previewId = merchantContentPublishService.resolveUserPreviewId(merchant);
        vo.setUserPreviewId(previewId);
        vo.setUserPreviewPath(buildUserPreviewPath(typeKey, previewId));
        return vo;
    }

    private String buildUserPreviewPath(String typeKey, Long previewId) {
        if (previewId == null) {
            return null;
        }
        switch (typeKey) {
            case "hotel":
                return "/mobile-design/hotel/detail?id=" + previewId;
            case "travel":
                return "/mobile-design/route/store?id=" + previewId;
            case "culture":
                return "/mobile-design/heritage/shop-detail?id=" + previewId;
            case "restaurant":
            case "goose":
                return "/mobile-design/goose/detail?id=" + previewId;
            default:
                return "/mobile-design/goose/detail?id=" + previewId;
        }
    }

    private MerchantStoreDecorVo.StoreTypeMeta buildStoreTypeMeta(String typeKey) {
        MerchantStoreDecorVo.StoreTypeMeta meta = new MerchantStoreDecorVo.StoreTypeMeta();
        meta.setKey(typeKey);
        meta.setLabel(MerchantStoreType.labelOf(typeKey));
        switch (typeKey) {
            case "restaurant":
            case "goose":
                meta.setIcon("coupon-fill");
                meta.setColor("#FF6600");
                meta.setBgColor("#FFF3EB");
                break;
            case "travel":
                meta.setIcon("map-fill");
                meta.setColor("#2EAF6A");
                meta.setBgColor("#E8F8EF");
                break;
            case "culture":
                meta.setIcon("gift-fill");
                meta.setColor("#B45D2D");
                meta.setBgColor("#F5EFEB");
                break;
            default:
                meta.setIcon("home-fill");
                meta.setColor("#2979FF");
                meta.setBgColor("#EAF2FF");
                break;
        }
        return meta;
    }

    private Map<String, Object> buildDefaultDecor(String typeKey, MerchantProfileVo profile) {
        Map<String, Object> defaults = new HashMap<>();
        String phone = StringUtils.defaultIfBlank(profile.getContactPhone(), "023-46781234");
        String hours = StringUtils.defaultIfBlank(profile.getStoreHours(), "09:00-21:00");

        if ("hotel".equals(typeKey)) {
            defaults.put("cover", "");
            defaults.put("bannerImages", new ArrayList<>());
            defaults.put("starLevel", 4);
            defaults.put("starLabel", "四星级");
            defaults.put("checkInTime", "14:00 后入住");
            defaults.put("checkOutTime", "12:00 前退房");
            defaults.put("phone", phone);
            defaults.put("facilities", defaultFacilities());
            defaults.put("hotelPolicy", HotelPolicyUtil.defaultPolicyMap());
        } else if ("restaurant".equals(typeKey) || "goose".equals(typeKey)) {
            defaults.put("cover", "");
            defaults.put("bannerList", new ArrayList<>());
            defaults.put("badgeText", "非遗美食");
            defaults.put("tags", asList("可堂食", "支持预约"));
            defaults.put("rating", "4.8");
            defaults.put("monthlySales", 0);
            defaults.put("intro", profile.getStoreName() + "欢迎您的光临，传承地道风味，支持在线预约到店用餐。");
            defaults.put("businessHours", defaultBusinessHours(hours));
        } else if ("travel".equals(typeKey)) {
            defaults.put("cover", "");
            defaults.put("bannerImages", new ArrayList<>());
            defaults.put("tags", asList("精品小团", "含讲解"));
            defaults.put("guides", new ArrayList<>());
            defaults.put("intro", profile.getStoreName() + "欢迎您，专业导游带队，安心出行。");
            defaults.put("wechatQrCode", "");
        } else {
            defaults.put("cover", "");
            defaults.put("bannerImages", new ArrayList<>());
            defaults.put("coverFrom", "#B45D2D");
            defaults.put("coverTo", "#8D4E2A");
            defaults.put("tags", asList("非遗文创", "可体验"));
            defaults.put("shipping", com.sqx.app.lvyou.heritage.shop.support.HeritageShippingUtil.toMap(
                    com.sqx.app.lvyou.heritage.shop.support.HeritageShippingUtil.defaultConfig()));
            defaults.put("intro", profile.getStoreName() + "专注非遗文创与手作体验。");
            defaults.put("introDetail", "");
            defaults.put("services", asList("文创销售", "手作体验", "团队预约"));
        }
        return defaults;
    }

    private List<Map<String, Object>> defaultFacilities() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(facility(1, "wifi", "免费WiFi"));
        list.add(facility(2, "car-fill", "免费停车"));
        list.add(facility(3, "grid-fill", "健身房"));
        list.add(facility(4, "gift-fill", "含早餐"));
        return list;
    }

    private Map<String, Object> facility(int id, String icon, String label) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("icon", icon);
        item.put("label", label);
        return item;
    }

    private List<Map<String, Object>> defaultBusinessHours(String hours) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> weekday = new HashMap<>();
        weekday.put("id", 1);
        weekday.put("label", "周一至周五");
        weekday.put("time", hours.replace("-", " - "));
        list.add(weekday);
        Map<String, Object> weekend = new HashMap<>();
        weekend.put("id", 2);
        weekend.put("label", "周六至周日");
        weekend.put("time", "09:00 - 22:00");
        list.add(weekend);
        return list;
    }

    private List<Map<String, Object>> defaultItinerary() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> node1 = new HashMap<>();
        node1.put("time", "09:00");
        node1.put("name", "集合出发");
        node1.put("location", "门店/集合点");
        node1.put("desc", "请准时到达集合地点");
        list.add(node1);
        Map<String, Object> node2 = new HashMap<>();
        node2.put("time", "12:00");
        node2.put("name", "核心体验");
        node2.put("location", "行程中途");
        node2.put("desc", "体验当地特色项目");
        list.add(node2);
        return list;
    }

    private Integer normalizeDayCount(Integer dayCount, String spec) {
        if (dayCount != null && dayCount > 0) {
            return dayCount;
        }
        if (StringUtils.isBlank(spec)) {
            return null;
        }
        String digits = spec.replaceAll("[^0-9]", "");
        if (StringUtils.isBlank(digits)) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(digits);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String serializeItineraryDays(List<Map<String, Object>> itineraryDays) {
        if (CollectionUtils.isEmpty(itineraryDays)) {
            return null;
        }
        List<Map<String, Object>> normalized = new ArrayList<>();
        int dayNo = 1;
        for (Map<String, Object> day : itineraryDays) {
            if (day == null) {
                continue;
            }
            String title = StringUtils.trimToEmpty(Objects.toString(day.get("title"),
                    Objects.toString(day.get("name"), "")));
            String content = StringUtils.trimToEmpty(Objects.toString(day.get("content"),
                    Objects.toString(day.get("desc"), "")));
            List<String> images = readStringListValue(day.get("images"));
            if (StringUtils.isBlank(title) && StringUtils.isBlank(content) && images.isEmpty()) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("dayNo", dayNo);
            item.put("dayLabel", StringUtils.defaultIfBlank(
                    StringUtils.trimToNull(Objects.toString(day.get("dayLabel"), null)),
                    "第" + dayNo + "天"));
            item.put("title", title);
            item.put("content", content);
            item.put("images", images);
            normalized.add(item);
            dayNo++;
        }
        if (normalized.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(normalized);
    }

    private String serializeBannerImages(List<String> banners) {
        List<String> normalized = readStringListValue(banners);
        if (normalized.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(normalized);
    }

    private List<String> parseBannerImages(String bannerImagesJson) {
        return readStringListValue(bannerImagesJson);
    }

    private List<Map<String, Object>> parseItineraryDays(String itineraryJson) {
        if (StringUtils.isBlank(itineraryJson)) {
            return Collections.emptyList();
        }
        try {
            List<Map<String, Object>> list = JSON.parseObject(itineraryJson,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            return list == null ? Collections.emptyList() : list;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> readStringListValue(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) value) {
                if (item != null && StringUtils.isNotBlank(String.valueOf(item))) {
                    result.add(String.valueOf(item).trim());
                }
            }
            return result;
        }
        if (value instanceof String && StringUtils.isNotBlank((String) value)) {
            String raw = ((String) value).trim();
            if (raw.startsWith("[")) {
                try {
                    List<String> parsed = JSON.parseArray(raw, String.class);
                    if (parsed != null) {
                        return parsed.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
                    }
                } catch (Exception ignored) {
                    // fall through
                }
            }
            String[] parts = raw.split("[,，、]");
            List<String> result = new ArrayList<>();
            for (String part : parts) {
                if (StringUtils.isNotBlank(part)) {
                    result.add(part.trim());
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private List<String> asList(String... values) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, values);
        return list;
    }

    private MerchantStoreDecorEntity findDecorEntity(Long merchantId) {
        LambdaQueryWrapper<MerchantStoreDecorEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantStoreDecorEntity::getMerchantId, merchantId)
                .last("LIMIT 1");
        return iMerchantStoreDecorService.getOne(queryWrapper, false);
    }

    private Map<String, Object> loadDecorMap(Long merchantId) {
        MerchantStoreDecorEntity entity = findDecorEntity(merchantId);
        if (entity == null || StringUtils.isBlank(entity.getDecorJson())) {
            return new LinkedHashMap<>();
        }
        try {
            return JSON.parseObject(entity.getDecorJson(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Object> mergeDecorMaps(Map<String, Object> defaults, Map<String, Object> stored) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (defaults != null) {
            merged.putAll(defaults);
        }
        if (stored == null || stored.isEmpty()) {
            return merged;
        }
        for (Map.Entry<String, Object> entry : stored.entrySet()) {
            if (!isBlankDecorValue(entry.getValue())) {
                merged.put(entry.getKey(), entry.getValue());
            }
        }
        return merged;
    }

    private boolean isBlankDecorValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return StringUtils.isBlank((String) value);
        }
        return false;
    }

    private void stripLegacyDecorServiceKeys(Map<String, Object> decor) {
        if (decor == null || decor.isEmpty()) {
            return;
        }
        LEGACY_DECOR_SERVICE_KEYS.forEach(decor::remove);
    }

    private void enrichHotelDecorFromPublished(Long merchantId, Map<String, Object> decor) {
        if (decor == null) {
            return;
        }
        LambdaQueryWrapper<HotelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelEntity::getMerchantId, merchantId)
                .orderByAsc(HotelEntity::getId)
                .last("LIMIT 1");
        HotelEntity hotel = iHotelService.getOne(queryWrapper, false);
        if (hotel == null) {
            return;
        }
        putIfBlankDecor(decor, "cover", hotel.getCoverImage());
        if (!decor.containsKey("hotelPolicy") || decor.get("hotelPolicy") == null) {
            HotelPolicyVo policyVo = HotelPolicyUtil.parse(hotel.getPolicyJson());
            Map<String, Object> policyMap = new LinkedHashMap<>();
            policyMap.put("bookingNotice", policyVo.getBookingNotice());
            policyMap.put("checkInNotice", policyVo.getCheckInNotice());
            policyMap.put("petPolicy", policyVo.getPetPolicy());
            policyMap.put("depositPolicy", policyVo.getDepositPolicy());
            policyMap.put("breakfastInfo", policyVo.getBreakfastInfo());
            decor.put("hotelPolicy", policyMap);
        }
        putIfBlankDecor(decor, "checkInTime", hotel.getCheckInTime());
        putIfBlankDecor(decor, "checkOutTime", hotel.getCheckOutTime());
        putIfBlankDecor(decor, "phone", hotel.getPhone());
        putIfBlankDecor(decor, "starLabel", hotel.getStarLabel());
        if (decor.get("starLevel") == null && hotel.getStarLevel() != null) {
            decor.put("starLevel", hotel.getStarLevel());
        }
    }

    private void putIfBlankDecor(Map<String, Object> decor, String key, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        Object existing = decor.get(key);
        if (existing == null || StringUtils.isBlank(String.valueOf(existing))) {
            decor.put(key, value);
        }
    }

    private MerchantApplyEntity findApprovedApply(Long merchantId) {
        LambdaQueryWrapper<MerchantApplyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantApplyEntity::getMerchantId, merchantId)
                .eq(MerchantApplyEntity::getAuditStatus, MerchantApplyAuditStatus.APPROVED.getCode())
                .orderByDesc(MerchantApplyEntity::getAuditedAt)
                .last("LIMIT 1");
        return iMerchantApplyService.getOne(queryWrapper, false);
    }

    private MerchantServiceEntity requireOwnedService(Long merchantId, Long serviceId) {
        MerchantServiceEntity entity = iMerchantServiceItemService.getById(serviceId);
        if (entity == null || !Objects.equals(entity.getMerchantId(), merchantId)) {
            throw new BizException(AppError.MERCHANT_SERVICE_NOT_FOUND);
        }
        return entity;
    }

    private void validateHomeTabKey(String status, String homeTabKey) {
        if (!MerchantServiceStatus.ON_SALE.getCode().equals(status)) {
            return;
        }
        if (StringUtils.isBlank(homeTabKey)) {
            throw new BizException(AppError.MERCHANT_HOME_TAB_REQUIRED);
        }
        if (!homeModuleService.isValidTabKey(homeTabKey)) {
            throw new BizException(AppError.MERCHANT_HOME_TAB_INVALID);
        }
    }

    private void applyRoomDetailFields(MerchantServiceEntity entity, MerchantEntity merchant, MerchantServiceSaveBo saveBo) {
        if (!MerchantStoreType.HOTEL.getCode().equalsIgnoreCase(StringUtils.defaultString(merchant.getStoreType()))) {
            return;
        }
        Map<String, Object> incomingRoomDetail = saveBo.getRoomDetail() == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(saveBo.getRoomDetail());
        boolean inheritStandard = Boolean.TRUE.equals(saveBo.getInheritStandard())
                || HotelRoomDetailUtil.isInheritStandard(incomingRoomDetail);
        Long roomStandardId = saveBo.getRoomStandardId();
        HotelRoomStandardEntity standard = null;
        Map<String, Object> roomDetail;
        if (inheritStandard && roomStandardId != null) {
            standard = requireOwnedRoomStandard(merchant, roomStandardId);
            roomDetail = HotelRoomDetailUtil.normalizeRoomOverrideDetail(incomingRoomDetail);
            roomDetail.put("inheritStandard", true);
            entity.setRoomStandardId(roomStandardId);
            entity.setRoomDetailJson(HotelRoomDetailUtil.toOverrideJson(roomDetail));
        } else {
            roomDetail = HotelRoomDetailUtil.mergeDetailMaps(HotelRoomDetailUtil.emptyDetailMap(), incomingRoomDetail);
            roomDetail.put("inheritStandard", false);
            entity.setRoomStandardId(roomStandardId);
            entity.setRoomDetailJson(HotelRoomDetailUtil.toJson(roomDetail));
        }
        Map<String, Object> effectiveDetail = HotelRoomDetailUtil.resolveEffectiveDetail(
                standard, entity.getRoomDetailJson(), saveBo.getDesc());
        String specSummary = HotelRoomDetailUtil.buildSpecSummary(effectiveDetail);
        if (StringUtils.isNotBlank(specSummary)) {
            entity.setSpec(specSummary);
        }
    }

    private void enrichHotelAvailableStock(MerchantServiceVo vo, MerchantServiceEntity entity) {
        if (vo == null || entity == null || entity.getId() == null) {
            return;
        }
        HotelRoomEntity room = findRoomByMerchantServiceId(entity.getId());
        if (room == null) {
            return;
        }
        vo.setStock(hotelInventorySupport.resolveAvailableForNight(room, LocalDate.now()));
    }

    private HotelRoomEntity findRoomByMerchantServiceId(Long serviceId) {
        LambdaQueryWrapper<HotelRoomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelRoomEntity::getMerchantServiceId, serviceId)
                .last("LIMIT 1");
        return iHotelRoomService.getOne(queryWrapper, false);
    }

    private MerchantServiceVo toServiceVo(MerchantServiceEntity entity) {
        MerchantServiceVo vo = new MerchantServiceVo();
        vo.setId(entity.getId());
        vo.setServiceCode(entity.getServiceCode());
        vo.setName(entity.getName());
        vo.setPrice(defaultDecimal(entity.getPrice()));
        vo.setStock(entity.getStock() == null ? 0 : entity.getStock());
        vo.setDesc(entity.getDescription());
        vo.setSpec(entity.getSpec());
        vo.setMeetPoint(entity.getMeetPoint());
        vo.setSpots(entity.getSpots());
        vo.setTravelCity(entity.getTravelCity());
        vo.setDayCount(entity.getDayCount());
        vo.setItineraryDays(parseItineraryDays(entity.getItineraryJson()));
        vo.setGoodsSpecs(entity.getGoodsSpecs());
        vo.setDetail(entity.getDetail());
        Map<String, Object> roomDetail = HotelRoomDetailUtil.parseRawMap(entity.getRoomDetailJson());
        boolean inheritStandard = HotelRoomDetailUtil.isInheritStandard(roomDetail);
        if (!inheritStandard) {
            roomDetail = HotelRoomDetailUtil.parseMap(entity.getRoomDetailJson());
        }
        vo.setRoomDetail(roomDetail);
        vo.setRoomStandardId(entity.getRoomStandardId());
        vo.setInheritStandard(inheritStandard);
        if (entity.getRoomStandardId() != null) {
            HotelRoomStandardEntity standard = iHotelRoomStandardService.getById(entity.getRoomStandardId());
            if (standard != null) {
                vo.setRoomStandardName(standard.getName());
            }
        }
        vo.setMonthlySales(entity.getMonthlySales() == null ? 0 : entity.getMonthlySales());
        vo.setCover(entity.getCoverImage());
        vo.setBanners(parseBannerImages(entity.getBannerImages()));
        vo.setDetailImages(parseBannerImages(entity.getDetailImages()));
        vo.setStatus(MerchantServiceStatus.normalize(entity.getStatus()));
        vo.setStatusLabel(MerchantServiceStatus.labelOf(entity.getStatus()));
        vo.setHomeTabKey(entity.getHomeTabKey());
        vo.setHomeTabLabel(homeModuleService.resolveTabLabel(entity.getHomeTabKey()));
        vo.setUpdatedAt(entity.getUpdateTime());
        return vo;
    }

    private void requireHotelMerchant(MerchantEntity merchant) {
        if (!MerchantStoreType.HOTEL.getCode().equalsIgnoreCase(StringUtils.defaultString(merchant.getStoreType()))) {
            throw new BizException(AppError.MERCHANT_STORE_TYPE_INVALID);
        }
    }

    private HotelEntity requireHotel(Long merchantId) {
        LambdaQueryWrapper<HotelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelEntity::getMerchantId, merchantId)
                .orderByDesc(HotelEntity::getId)
                .last("LIMIT 1");
        HotelEntity hotel = iHotelService.getOne(queryWrapper, false);
        if (hotel == null) {
            throw new BizException(AppError.HOTEL_NOT_FOUND);
        }
        return hotel;
    }

    private HotelRoomStandardEntity requireOwnedRoomStandard(MerchantEntity merchant, Long standardId) {
        HotelRoomStandardEntity entity = iHotelRoomStandardService.getById(standardId);
        if (entity == null || !Objects.equals(entity.getMerchantId(), merchant.getId())) {
            throw new BizException(AppError.HOTEL_ROOM_STANDARD_NOT_FOUND);
        }
        return entity;
    }

    private HotelRoomStandardVo toRoomStandardVo(HotelRoomStandardEntity entity) {
        HotelRoomStandardVo vo = new HotelRoomStandardVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setFloorHint(entity.getFloorHint());
        vo.setFacilities(HotelRoomStandardUtil.parseFacilitiesJson(entity.getFacilitiesJson()));
        vo.setDefaults(HotelRoomStandardUtil.parseDefaults(entity.getDefaultsJson()));
        vo.setGallery(HotelRoomStandardUtil.parseStringListJson(entity.getGalleryJson()));
        vo.setTags(HotelRoomStandardUtil.parseStringListJson(entity.getTagsJson()));
        vo.setSort(entity.getSort());
        vo.setStatus(entity.getStatus());
        vo.setUpdatedAt(entity.getUpdateTime());
        vo.setLinkedRoomCount(countLinkedRoomStandards(entity.getId()));
        return vo;
    }

    private int countLinkedRoomStandards(Long standardId) {
        LambdaQueryWrapper<MerchantServiceEntity> serviceQuery = new LambdaQueryWrapper<>();
        serviceQuery.eq(MerchantServiceEntity::getRoomStandardId, standardId);
        long serviceCount = iMerchantServiceItemService.count(serviceQuery);
        LambdaQueryWrapper<HotelRoomEntity> roomQuery = new LambdaQueryWrapper<>();
        roomQuery.eq(HotelRoomEntity::getRoomStandardId, standardId);
        long roomCount = iHotelRoomService.count(roomQuery);
        return (int) Math.max(serviceCount, roomCount);
    }

    /** 客房标准变更后，同步关联房型到用户端展示 */
    private void syncLinkedRoomStandardServices(MerchantEntity merchant, Long standardId) {
        if (standardId == null) {
            return;
        }
        LambdaQueryWrapper<MerchantServiceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantServiceEntity::getMerchantId, merchant.getId())
                .eq(MerchantServiceEntity::getRoomStandardId, standardId);
        List<MerchantServiceEntity> services = iMerchantServiceItemService.list(queryWrapper);
        if (CollectionUtils.isEmpty(services)) {
            return;
        }
        for (MerchantServiceEntity service : services) {
            merchantContentPublishService.syncService(merchant, service);
        }
    }

    private long countServices(Long merchantId, String status) {
        LambdaQueryWrapper<MerchantServiceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantServiceEntity::getMerchantId, merchantId);
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(MerchantServiceEntity::getStatus, status);
        }
        return iMerchantServiceItemService.count(queryWrapper);
    }

    private long countOrders(Long merchantId, List<Integer> statuses, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getMerchantId, merchantId);
        if (CollectionUtils.isNotEmpty(statuses)) {
            queryWrapper.in(OrderEntity::getStatus, statuses);
        }
        if (start != null) {
            queryWrapper.ge(OrderEntity::getCreateTime, start);
        }
        if (end != null) {
            queryWrapper.le(OrderEntity::getCreateTime, end);
        }
        return iOrderService.count(queryWrapper);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private String generateServiceCode() {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "MS" + LocalDateTime.now().format(SERVICE_CODE_FORMAT) + suffix;
    }
}

package com.sqx.app.lvyou.merchant.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.destination.support.DestinationDisplaySupport;
import com.sqx.app.lvyou.destination.support.PublishDestinationResolver;
import com.sqx.app.lvyou.heritage.shop.service.HeritageShopCategoryService;
import com.sqx.app.lvyou.heritage.shop.service.HeritageShopService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantApplyService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.hotel.db.service.IHotelService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelEntity;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantAdjustBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantApplyAuditBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantApplyBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantApplyPageBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantInfoBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantPageBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantSaveBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantUpdateBo;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantApplyEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantAdminVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantApplyVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantStatusVo;
import com.sqx.app.lvyou.merchant.enums.MerchantAppStatus;
import com.sqx.app.lvyou.merchant.enums.MerchantApplyAuditStatus;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import com.sqx.app.lvyou.merchant.service.MerchantService;
import com.sqx.app.lvyou.merchant.service.MerchantWalletService;
import com.sqx.app.lvyou.merchant.support.MerchantApplySupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 商户业务服务实现
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    private static final int MERCHANT_STATUS_NORMAL = 1;
    private static final int MERCHANT_STATUS_DISABLED = 2;
    private static final int MERCHANT_OPEN = 1;
    private static final int MERCHANT_CLOSED = 0;
    private static final int HOME_RECOMMEND_ENABLED = 1;
    private static final int HOME_RECOMMEND_DISABLED = 0;
    private static final int AUDIT_PENDING = MerchantApplyAuditStatus.PENDING.getCode();
    private static final int AUDIT_APPROVED = MerchantApplyAuditStatus.APPROVED.getCode();
    private static final int AUDIT_REJECTED = MerchantApplyAuditStatus.REJECTED.getCode();
    private static final DateTimeFormatter APPLY_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private IMerchantApplyService iMerchantApplyService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private IOrderService iOrderService;

    @Resource
    private IHotelService iHotelService;

    @Resource
    private PublishDestinationResolver publishDestinationResolver;

    @Resource
    private MerchantWalletService merchantWalletService;

    @Resource
    private HeritageShopCategoryService heritageShopCategoryService;

    @Resource
    private HeritageShopService heritageShopService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(Long userId, MerchantApplyBo applyBo) {
        UserEntity user = requireUser(userId);
        if (findActiveMerchantByUserId(userId) != null) {
            throw new BizException(AppError.MERCHANT_ALREADY_EXISTS);
        }
        if (findPendingApply(userId) != null) {
            throw new BizException(AppError.MERCHANT_APPLY_PENDING);
        }
        MerchantApplySupport.validateApplyPayload(applyBo);
        validateCultureShopCategory(applyBo);
        MerchantApplySupport.requireCoordinates(applyBo.getLatitude(), applyBo.getLongitude());
        Long destinationId = publishDestinationResolver.requireFromCoordinates(applyBo.getLatitude(), applyBo.getLongitude());

        MerchantApplyEntity rejectedApply = findLatestRejectedApply(userId);
        if (rejectedApply != null) {
            populateApplyEntity(rejectedApply, applyBo, destinationId);
            rejectedApply.setAuditStatus(AUDIT_PENDING);
            rejectedApply.setAuditRemark(null);
            rejectedApply.setAuditedAt(null);
            rejectedApply.setSubmittedAt(LocalDateTime.now());
            iMerchantApplyService.updateById(rejectedApply);
            return;
        }

        MerchantApplyEntity entity = new MerchantApplyEntity();
        entity.setApplyNo(generateApplyNo());
        entity.setUserId(userId);
        populateApplyEntity(entity, applyBo, destinationId);
        entity.setAuditStatus(AUDIT_PENDING);
        entity.setSubmittedAt(LocalDateTime.now());
        iMerchantApplyService.save(entity);
    }

    @Override
    public MerchantStatusVo getStatus(Long userId) {
        requireUser(userId);

        MerchantApplyEntity latestApply = findLatestApply(userId);
        if (latestApply != null) {
            MerchantApplyAuditStatus auditStatus = MerchantApplyAuditStatus.fromCode(latestApply.getAuditStatus());
            if (auditStatus == MerchantApplyAuditStatus.REJECTED) {
                return buildSimpleStatus(MerchantAppStatus.REJECTED, latestApply, null);
            }
            if (auditStatus == MerchantApplyAuditStatus.PENDING) {
                return buildSimpleStatus(MerchantAppStatus.PENDING, latestApply, null);
            }
        }

        MerchantEntity merchant = findActiveMerchantByUserId(userId);
        if (merchant != null) {
            return buildApprovedStatus(merchant, latestApply);
        }

        if (latestApply != null) {
            MerchantApplyAuditStatus auditStatus = MerchantApplyAuditStatus.fromCode(latestApply.getAuditStatus());
            if (auditStatus == MerchantApplyAuditStatus.APPROVED) {
                MerchantEntity linkedMerchant = latestApply.getMerchantId() == null
                        ? null
                        : iMerchantService.getById(latestApply.getMerchantId());
                if (linkedMerchant != null && Objects.equals(linkedMerchant.getStatus(), MERCHANT_STATUS_NORMAL)) {
                    return buildApprovedStatus(linkedMerchant, latestApply);
                }
                return buildSimpleStatus(MerchantAppStatus.APPROVED, latestApply, null);
            }
        }

        return buildSimpleStatus(MerchantAppStatus.NONE, null, null);
    }

    @Override
    public IPage<MerchantApplyVo> pageApply(MerchantApplyPageBo pageBo) {
        Page<MerchantApplyEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<MerchantApplyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(pageBo.getId()), MerchantApplyEntity::getId, pageBo.getId())
                .eq(StringUtils.isNotBlank(pageBo.getApplyNo()), MerchantApplyEntity::getApplyNo, pageBo.getApplyNo())
                .like(StringUtils.isNotBlank(pageBo.getStoreName()), MerchantApplyEntity::getStoreName, pageBo.getStoreName())
                .like(StringUtils.isNotBlank(pageBo.getContactName()), MerchantApplyEntity::getContactName, pageBo.getContactName())
                .like(StringUtils.isNotBlank(pageBo.getContactPhone()), MerchantApplyEntity::getContactPhone, pageBo.getContactPhone())
                .eq(StringUtils.isNotBlank(pageBo.getStoreType()), MerchantApplyEntity::getStoreType, MerchantStoreType.normalize(pageBo.getStoreType()))
                .eq(Objects.nonNull(pageBo.getAuditStatus()), MerchantApplyEntity::getAuditStatus, pageBo.getAuditStatus())
                .eq(Objects.nonNull(pageBo.getDestinationId()), MerchantApplyEntity::getDestinationId, pageBo.getDestinationId())
                .orderByDesc(MerchantApplyEntity::getSubmittedAt)
                .orderByDesc(MerchantApplyEntity::getId);

        IPage<MerchantApplyEntity> entityPage = iMerchantApplyService.page(page, queryWrapper);
        Map<Long, UserEntity> userMap = loadUserMap(entityPage.getRecords());
        Map<Long, DestinationEntity> destinationMap = loadDestinationMap(entityPage.getRecords());
        Map<Long, MerchantEntity> merchantMap = loadMerchantMap(entityPage.getRecords());

        List<MerchantApplyVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (MerchantApplyEntity record : entityPage.getRecords()) {
                voList.add(toApplyVo(record, userMap, destinationMap, merchantMap));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public MerchantApplyVo getApplyDetail(Long id) {
        MerchantApplyEntity entity = iMerchantApplyService.getById(id);
        if (entity == null) {
            throw new BizException(AppError.MERCHANT_APPLY_NOT_FOUND);
        }
        Map<Long, UserEntity> userMap = loadUserMap(Collections.singletonList(entity));
        Map<Long, DestinationEntity> destinationMap = loadDestinationMap(Collections.singletonList(entity));
        Map<Long, MerchantEntity> merchantMap = loadMerchantMap(Collections.singletonList(entity));
        return toApplyVo(entity, userMap, destinationMap, merchantMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(MerchantApplyAuditBo auditBo) {
        MerchantApplyEntity apply = iMerchantApplyService.getById(auditBo.getId());
        if (apply == null) {
            throw new BizException(AppError.MERCHANT_APPLY_NOT_FOUND);
        }
        if (!Objects.equals(apply.getAuditStatus(), AUDIT_PENDING)
                && !Objects.equals(apply.getAuditStatus(), AUDIT_REJECTED)) {
            throw new BizException(AppError.MERCHANT_APPLY_ALREADY_AUDITED);
        }

        String action = StringUtils.lowerCase(StringUtils.trimToEmpty(auditBo.getAction()));
        if ("pass".equals(action)) {
            approveApply(apply);
            return;
        }
        if ("reject".equals(action)) {
            rejectApply(apply, auditBo.getReason());
            return;
        }
        throw new BizException(AppError.MERCHANT_AUDIT_ACTION_INVALID);
    }

    @Override
    public IPage<MerchantAdminVo> pageMerchant(MerchantPageBo pageBo) {
        Page<MerchantEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(pageBo.getId()), MerchantEntity::getId, pageBo.getId())
                .eq(StringUtils.isNotBlank(pageBo.getMerchantCode()), MerchantEntity::getMerchantCode, pageBo.getMerchantCode())
                .like(StringUtils.isNotBlank(pageBo.getStoreName()), MerchantEntity::getStoreName, pageBo.getStoreName())
                .like(StringUtils.isNotBlank(pageBo.getContactPhone()), MerchantEntity::getContactPhone, pageBo.getContactPhone())
                .eq(StringUtils.isNotBlank(pageBo.getStoreType()), MerchantEntity::getStoreType, MerchantStoreType.normalize(pageBo.getStoreType()))
                .eq(Objects.nonNull(pageBo.getStatus()), MerchantEntity::getStatus, pageBo.getStatus())
                .eq(Objects.nonNull(pageBo.getIsOpen()), MerchantEntity::getIsOpen, pageBo.getIsOpen())
                .eq(Objects.nonNull(pageBo.getDestinationId()), MerchantEntity::getDestinationId, pageBo.getDestinationId())
                .orderByDesc(MerchantEntity::getCreateTime)
                .orderByDesc(MerchantEntity::getId);

        IPage<MerchantEntity> entityPage = iMerchantService.page(page, queryWrapper);
        Map<Long, UserEntity> userMap = loadUserMapByMerchants(entityPage.getRecords());
        Map<Long, DestinationEntity> destinationMap = loadDestinationMapByMerchants(entityPage.getRecords());

        List<MerchantAdminVo> voList = new ArrayList<>();
        List<Long> merchantIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (MerchantEntity record : entityPage.getRecords()) {
                voList.add(toMerchantAdminVo(record, userMap, destinationMap));
                merchantIds.add(record.getId());
            }
        }
        merchantWalletService.fillAdminDepositFields(voList, merchantIds);
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public MerchantAdminVo getMerchantDetail(Long id) {
        MerchantEntity entity = iMerchantService.getById(id);
        if (entity == null) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }
        Map<Long, UserEntity> userMap = loadUserMapByMerchants(Collections.singletonList(entity));
        Map<Long, DestinationEntity> destinationMap = loadDestinationMapByMerchants(Collections.singletonList(entity));
        MerchantAdminVo vo = toMerchantAdminVo(entity, userMap, destinationMap);
        merchantWalletService.fillAdminDepositFields(vo, entity.getId());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantAdminVo adjust(MerchantAdjustBo adjustBo) {
        MerchantEntity merchant = iMerchantService.getById(adjustBo.getId());
        if (merchant == null) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }

        String action = StringUtils.lowerCase(StringUtils.trimToEmpty(adjustBo.getAction()));
        LambdaUpdateWrapper<MerchantEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantEntity::getId, merchant.getId());

        if ("enable".equals(action)) {
            updateWrapper.set(MerchantEntity::getStatus, MERCHANT_STATUS_NORMAL);
            iMerchantService.update(new MerchantEntity(), updateWrapper);
        } else if ("disable".equals(action)) {
            updateWrapper.set(MerchantEntity::getStatus, MERCHANT_STATUS_DISABLED);
            iMerchantService.update(new MerchantEntity(), updateWrapper);
        } else if ("open".equals(action)) {
            updateWrapper.set(MerchantEntity::getIsOpen, MERCHANT_OPEN);
            iMerchantService.update(new MerchantEntity(), updateWrapper);
        } else if ("close".equals(action)) {
            updateWrapper.set(MerchantEntity::getIsOpen, MERCHANT_CLOSED);
            iMerchantService.update(new MerchantEntity(), updateWrapper);
        } else if ("homerecommendon".equals(action)) {
            updateWrapper.set(MerchantEntity::getHomeRecommendEnabled, HOME_RECOMMEND_ENABLED);
            iMerchantService.update(new MerchantEntity(), updateWrapper);
        } else if ("homerecommendoff".equals(action)) {
            updateWrapper.set(MerchantEntity::getHomeRecommendEnabled, HOME_RECOMMEND_DISABLED);
            iMerchantService.update(new MerchantEntity(), updateWrapper);
        } else {
            throw new BizException(AppError.MERCHANT_ADJUST_ACTION_INVALID);
        }

        return getMerchantDetail(merchant.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMerchant(MerchantSaveBo saveBo) {
        MerchantApplySupport.validateApplyPayload(saveBo);
        validateCultureShopCategory(saveBo);
        MerchantApplySupport.requireCoordinates(saveBo.getLatitude(), saveBo.getLongitude());
        Long destinationId = publishDestinationResolver.requireFromCoordinates(saveBo.getLatitude(), saveBo.getLongitude());
        ensureUserAvailableForMerchant(saveBo.getUserId(), null);

        MerchantEntity merchant = buildMerchantEntity(saveBo);
        merchant.setMerchantCode(generateMerchantCode());
        merchant.setUserId(saveBo.getUserId());
        merchant.setDestinationId(destinationId);
        merchant.setLatitude(saveBo.getLatitude());
        merchant.setLongitude(saveBo.getLongitude());
        merchant.setStatus(saveBo.getStatus() == null ? MERCHANT_STATUS_NORMAL : saveBo.getStatus());
        merchant.setIsOpen(saveBo.getIsOpen() == null ? MERCHANT_OPEN : saveBo.getIsOpen());
        iMerchantService.save(merchant);
        incrementDestinationMerchantCount(destinationId);
        linkPendingApplyIfNeeded(saveBo.getUserId(), merchant.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchant(MerchantUpdateBo updateBo) {
        MerchantEntity existing = iMerchantService.getById(updateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }
        MerchantApplySupport.validateApplyPayload(updateBo);
        validateCultureShopCategory(updateBo);
        MerchantApplySupport.requireCoordinates(updateBo.getLatitude(), updateBo.getLongitude());
        Long destinationId = publishDestinationResolver.requireFromCoordinates(updateBo.getLatitude(), updateBo.getLongitude());
        ensureUserAvailableForMerchant(updateBo.getUserId(), existing.getId());

        Long oldDestinationId = existing.getDestinationId();
        applyMerchantPayload(existing, updateBo);
        existing.setUserId(updateBo.getUserId());
        existing.setDestinationId(destinationId);
        existing.setLatitude(updateBo.getLatitude());
        existing.setLongitude(updateBo.getLongitude());
        if (updateBo.getStatus() != null) {
            existing.setStatus(updateBo.getStatus());
        }
        if (updateBo.getIsOpen() != null) {
            existing.setIsOpen(updateBo.getIsOpen());
        }
        iMerchantService.updateById(existing);
        if (!Objects.equals(oldDestinationId, destinationId)) {
            decrementDestinationMerchantCount(oldDestinationId);
            incrementDestinationMerchantCount(destinationId);
        }
        syncCultureMerchantShopCategory(existing.getId(), existing.getStoreType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMerchant(MerchantInfoBo infoBo) {
        MerchantEntity existing = iMerchantService.getById(infoBo.getId());
        if (existing == null) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }
        ensureMerchantDeletable(existing.getId());
        iMerchantService.removeById(existing.getId());
        decrementDestinationMerchantCount(existing.getDestinationId());
    }

    private void ensureUserAvailableForMerchant(Long userId, Long excludeMerchantId) {
        if (userId == null) {
            return;
        }
        requireUser(userId);
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantEntity::getUserId, userId);
        if (excludeMerchantId != null) {
            queryWrapper.ne(MerchantEntity::getId, excludeMerchantId);
        }
        if (iMerchantService.count(queryWrapper) > 0) {
            throw new BizException(AppError.MERCHANT_USER_ALREADY_BOUND);
        }
    }

    private void ensureMerchantDeletable(Long merchantId) {
        LambdaQueryWrapper<OrderEntity> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(OrderEntity::getMerchantId, merchantId)
                .notIn(OrderEntity::getStatus, MerchantOrderStatusGroup.CLOSED);
        if (iOrderService.count(orderQuery) > 0) {
            throw new BizException(AppError.MERCHANT_HAS_RELATED_DATA);
        }
        LambdaQueryWrapper<HotelEntity> hotelQuery = new LambdaQueryWrapper<>();
        hotelQuery.eq(HotelEntity::getMerchantId, merchantId);
        if (iHotelService.count(hotelQuery) > 0) {
            throw new BizException(AppError.MERCHANT_HAS_RELATED_DATA);
        }
    }

    private void decrementDestinationMerchantCount(Long destinationId) {
        if (destinationId == null) {
            return;
        }
        DestinationEntity destination = iDestinationService.getById(destinationId);
        if (destination == null) {
            return;
        }
        int current = destination.getMerchantCount() == null ? 0 : destination.getMerchantCount();
        int nextCount = Math.max(0, current - 1);
        LambdaUpdateWrapper<DestinationEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DestinationEntity::getId, destinationId)
                .set(DestinationEntity::getMerchantCount, nextCount);
        iDestinationService.update(new DestinationEntity(), updateWrapper);
    }

    private void approveApply(MerchantApplyEntity apply) {
        MerchantEntity existingMerchant = findMerchantByUserId(apply.getUserId());
        if (existingMerchant != null) {
            if (Objects.equals(existingMerchant.getStatus(), MERCHANT_STATUS_NORMAL)) {
                throw new BizException(AppError.MERCHANT_ALREADY_EXISTS);
            }
            reactivateMerchantFromApply(existingMerchant, apply);
            markApplyApproved(apply, existingMerchant.getId());
            return;
        }

        MerchantEntity merchant = buildMerchantEntity(toApplyBo(apply));
        merchant.setMerchantCode(generateMerchantCode());
        merchant.setUserId(apply.getUserId());
        merchant.setDestinationId(apply.getDestinationId());
        merchant.setLatitude(apply.getLatitude());
        merchant.setLongitude(apply.getLongitude());
        merchant.setStatus(MERCHANT_STATUS_NORMAL);
        merchant.setIsOpen(MERCHANT_OPEN);
        iMerchantService.save(merchant);

        markApplyApproved(apply, merchant.getId());
        incrementDestinationMerchantCount(merchant.getDestinationId());
        syncCultureMerchantShopCategory(merchant.getId(), merchant.getStoreType());
    }

    private void rejectApply(MerchantApplyEntity apply, String reason) {
        if (StringUtils.isBlank(reason)) {
            throw new BizException(AppError.MERCHANT_AUDIT_REASON_REQUIRED);
        }
        cleanupMerchantForRejectedApply(apply);
        LocalDateTime auditedAt = LocalDateTime.now();
        LambdaUpdateWrapper<MerchantApplyEntity> applyUpdate = new LambdaUpdateWrapper<>();
        applyUpdate.eq(MerchantApplyEntity::getId, apply.getId())
                .set(MerchantApplyEntity::getAuditStatus, AUDIT_REJECTED)
                .set(MerchantApplyEntity::getAuditRemark, StringUtils.trim(reason))
                .set(MerchantApplyEntity::getAuditedAt, auditedAt)
                .set(MerchantApplyEntity::getMerchantId, null);
        iMerchantApplyService.update(new MerchantApplyEntity(), applyUpdate);
    }

    private void markApplyApproved(MerchantApplyEntity apply, Long merchantId) {
        LocalDateTime auditedAt = LocalDateTime.now();
        LambdaUpdateWrapper<MerchantApplyEntity> applyUpdate = new LambdaUpdateWrapper<>();
        applyUpdate.eq(MerchantApplyEntity::getId, apply.getId())
                .set(MerchantApplyEntity::getAuditStatus, AUDIT_APPROVED)
                .set(MerchantApplyEntity::getAuditRemark, null)
                .set(MerchantApplyEntity::getMerchantId, merchantId)
                .set(MerchantApplyEntity::getAuditedAt, auditedAt);
        iMerchantApplyService.update(new MerchantApplyEntity(), applyUpdate);
    }

    private void reactivateMerchantFromApply(MerchantEntity merchant, MerchantApplyEntity apply) {
        Long oldDestinationId = merchant.getDestinationId();
        applyMerchantPayload(merchant, toApplyBo(apply));
        merchant.setDestinationId(apply.getDestinationId());
        merchant.setLatitude(apply.getLatitude());
        merchant.setLongitude(apply.getLongitude());
        merchant.setStatus(MERCHANT_STATUS_NORMAL);
        merchant.setIsOpen(MERCHANT_OPEN);
        iMerchantService.updateById(merchant);
        if (!Objects.equals(oldDestinationId, apply.getDestinationId())) {
            decrementDestinationMerchantCount(oldDestinationId);
            incrementDestinationMerchantCount(apply.getDestinationId());
        }
        syncCultureMerchantShopCategory(merchant.getId(), merchant.getStoreType());
    }

    private void cleanupMerchantForRejectedApply(MerchantApplyEntity apply) {
        MerchantEntity merchant = resolveLinkedMerchant(apply);
        if (merchant == null) {
            return;
        }
        if (isMerchantDeletable(merchant.getId())) {
            iMerchantService.removeById(merchant.getId());
            decrementDestinationMerchantCount(merchant.getDestinationId());
            return;
        }
        merchant.setStatus(MERCHANT_STATUS_DISABLED);
        merchant.setIsOpen(MERCHANT_CLOSED);
        iMerchantService.updateById(merchant);
    }

    private MerchantEntity resolveLinkedMerchant(MerchantApplyEntity apply) {
        if (apply.getMerchantId() != null) {
            MerchantEntity merchant = iMerchantService.getById(apply.getMerchantId());
            if (merchant != null) {
                return merchant;
            }
        }
        return findMerchantByUserId(apply.getUserId());
    }

    private boolean isMerchantDeletable(Long merchantId) {
        LambdaQueryWrapper<OrderEntity> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(OrderEntity::getMerchantId, merchantId)
                .notIn(OrderEntity::getStatus, MerchantOrderStatusGroup.CLOSED);
        if (iOrderService.count(orderQuery) > 0) {
            return false;
        }
        LambdaQueryWrapper<HotelEntity> hotelQuery = new LambdaQueryWrapper<>();
        hotelQuery.eq(HotelEntity::getMerchantId, merchantId);
        return iHotelService.count(hotelQuery) <= 0;
    }

    private MerchantStatusVo buildApprovedStatus(MerchantEntity merchant, MerchantApplyEntity apply) {
        MerchantStatusVo vo = buildSimpleStatus(MerchantAppStatus.APPROVED, apply, merchant);
        vo.setMerchantId(merchant.getId());
        vo.setMerchantCode(merchant.getMerchantCode());
        vo.setStoreName(merchant.getStoreName());
        vo.setStoreType(merchant.getStoreType());
        vo.setStoreTypeLabel(MerchantStoreType.labelOf(merchant.getStoreType()));
        return vo;
    }

    private MerchantStatusVo buildSimpleStatus(MerchantAppStatus appStatus, MerchantApplyEntity apply, MerchantEntity merchant) {
        MerchantStatusVo vo = new MerchantStatusVo();
        vo.setStatus(appStatus.getCode());
        vo.setStatusLabel(appStatus.getLabel());
        if (apply != null) {
            vo.setApplyNo(apply.getApplyNo());
            vo.setStoreName(apply.getStoreName());
            vo.setStoreType(apply.getStoreType());
            vo.setStoreTypeLabel(MerchantStoreType.labelOf(apply.getStoreType()));
            vo.setAuditRemark(apply.getAuditRemark());
            vo.setSubmittedAt(apply.getSubmittedAt());
            vo.setAuditedAt(apply.getAuditedAt());
            if (apply.getMerchantId() != null) {
                vo.setMerchantId(apply.getMerchantId());
            }
            vo.setApplyDetail(buildApplyDetail(apply));
        }
        if (merchant != null) {
            vo.setMerchantId(merchant.getId());
            vo.setMerchantCode(merchant.getMerchantCode());
        }
        return vo;
    }

    private MerchantStatusVo.MerchantApplyDetailVo buildApplyDetail(MerchantApplyEntity apply) {
        MerchantStatusVo.MerchantApplyDetailVo detail = new MerchantStatusVo.MerchantApplyDetailVo();
        detail.setContactName(apply.getContactName());
        detail.setContactPhone(apply.getContactPhone());
        detail.setStoreRegion(apply.getStoreRegion());
        detail.setStoreAddressDetail(resolveApplyAddressDetail(apply));
        detail.setStoreAddress(apply.getStoreAddress());
        detail.setLatitude(apply.getLatitude());
        detail.setLongitude(apply.getLongitude());
        detail.setStoreHours(apply.getStoreHours());
        detail.setShopCategory(apply.getShopCategory());
        detail.setShopCategoryLabel(resolveShopCategoryLabel(apply.getShopCategory()));
        detail.setLicenseNo(apply.getLicenseNo());
        detail.setLicenseImage(apply.getLicenseImage());
        detail.setLegalName(apply.getLegalName());
        detail.setLegalIdNo(apply.getLegalIdNo());
        detail.setLegalIdFront(apply.getLegalIdFront());
        detail.setLegalIdBack(apply.getLegalIdBack());
        detail.setQualImages(parseQualImages(apply.getQualImages()));
        return detail;
    }

    private void populateApplyEntity(MerchantApplyEntity entity, MerchantApplyBo applyBo, Long destinationId) {
        entity.setDestinationId(destinationId);
        entity.setStoreName(StringUtils.trim(applyBo.getStoreName()));
        entity.setStoreType(MerchantStoreType.normalize(applyBo.getStoreType()));
        entity.setShopCategory(normalizeShopCategory(applyBo.getShopCategory()));
        entity.setCraftId(null);
        entity.setContactName(StringUtils.trim(applyBo.getContactName()));
        entity.setContactPhone(StringUtils.trim(applyBo.getContactPhone()));
        entity.setStoreRegion(StringUtils.trim(applyBo.getStoreRegion()));
        entity.setStoreAddress(resolveStoreAddress(applyBo));
        entity.setLatitude(applyBo.getLatitude());
        entity.setLongitude(applyBo.getLongitude());
        entity.setStoreHours(StringUtils.defaultIfBlank(StringUtils.trimToNull(applyBo.getStoreHours()), "09:00-21:00"));
        entity.setLicenseNo(StringUtils.trim(applyBo.getLicenseNo()));
        entity.setLicenseImage(StringUtils.trim(applyBo.getLicenseImage()));
        entity.setLegalName(StringUtils.trim(applyBo.getLegalName()));
        entity.setLegalIdNo(StringUtils.trim(applyBo.getLegalIdNo()));
        entity.setLegalIdFront(StringUtils.trim(applyBo.getLegalIdFront()));
        entity.setLegalIdBack(StringUtils.trim(applyBo.getLegalIdBack()));
        entity.setQualImages(serializeQualImages(applyBo.getQualImages()));
    }

    private MerchantApplyEntity findLatestRejectedApply(Long userId) {
        MerchantApplyEntity latestApply = findLatestApply(userId);
        if (latestApply == null || !Objects.equals(latestApply.getAuditStatus(), AUDIT_REJECTED)) {
            return null;
        }
        return latestApply;
    }

    private String resolveApplyAddressDetail(MerchantApplyEntity apply) {
        if (apply == null) {
            return null;
        }
        MerchantEntity probe = new MerchantEntity();
        probe.setStoreRegion(apply.getStoreRegion());
        probe.setStoreAddress(apply.getStoreAddress());
        return resolveStoreAddressDetail(probe);
    }

    private MerchantApplyVo toApplyVo(MerchantApplyEntity entity,
                                      Map<Long, UserEntity> userMap,
                                      Map<Long, DestinationEntity> destinationMap,
                                      Map<Long, MerchantEntity> merchantMap) {
        MerchantApplyVo vo = new MerchantApplyVo();
        vo.setId(entity.getId());
        vo.setApplyNo(entity.getApplyNo());
        vo.setUserId(entity.getUserId());
        vo.setDestinationId(entity.getDestinationId());
        vo.setStoreName(entity.getStoreName());
        vo.setStoreType(entity.getStoreType());
        vo.setStoreTypeLabel(MerchantStoreType.labelOf(entity.getStoreType()));
        vo.setShopCategory(entity.getShopCategory());
        vo.setShopCategoryLabel(resolveShopCategoryLabel(entity.getShopCategory()));
        vo.setContactName(entity.getContactName());
        vo.setContactPhone(entity.getContactPhone());
        vo.setStoreRegion(entity.getStoreRegion());
        vo.setStoreAddress(entity.getStoreAddress());
        vo.setLatitude(entity.getLatitude());
        vo.setLongitude(entity.getLongitude());
        vo.setStoreHours(entity.getStoreHours());
        vo.setLicenseNo(entity.getLicenseNo());
        vo.setLicenseImage(entity.getLicenseImage());
        vo.setLegalName(entity.getLegalName());
        vo.setLegalIdNo(entity.getLegalIdNo());
        vo.setLegalIdFront(entity.getLegalIdFront());
        vo.setLegalIdBack(entity.getLegalIdBack());
        vo.setQualImages(parseQualImages(entity.getQualImages()));
        vo.setAuditStatus(entity.getAuditStatus());
        MerchantApplyAuditStatus auditStatus = MerchantApplyAuditStatus.fromCode(entity.getAuditStatus());
        vo.setAuditStatusLabel(auditStatus == null ? null : auditStatus.getLabel());
        vo.setAuditRemark(entity.getAuditRemark());
        vo.setMerchantId(entity.getMerchantId());
        vo.setSubmittedAt(entity.getSubmittedAt());
        vo.setAuditedAt(entity.getAuditedAt());
        vo.setCreateTime(entity.getCreateTime());

        UserEntity user = userMap.get(entity.getUserId());
        if (user != null) {
            vo.setUserPhone(user.getPhone());
            vo.setUserName(user.getUserName());
        }
        DestinationEntity destination = destinationMap.get(entity.getDestinationId());
        vo.setDestinationName(DestinationDisplaySupport.resolveLabel(
                destination, entity.getStoreAddress(), entity.getStoreRegion()));
        if (entity.getMerchantId() != null) {
            MerchantEntity merchant = merchantMap.get(entity.getMerchantId());
            if (merchant != null) {
                vo.setMerchantCode(merchant.getMerchantCode());
            }
        }
        return vo;
    }

    private Map<Long, UserEntity> loadUserMap(List<MerchantApplyEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> userIds = records.stream()
                .map(MerchantApplyEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<UserEntity> users = iUserService.listByIds(userIds);
        Map<Long, UserEntity> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(users)) {
            for (UserEntity user : users) {
                map.put(user.getUserId(), user);
            }
        }
        return map;
    }

    private Map<Long, DestinationEntity> loadDestinationMap(List<MerchantApplyEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> destinationIds = records.stream()
                .map(MerchantApplyEntity::getDestinationId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(destinationIds)) {
            return Collections.emptyMap();
        }
        List<DestinationEntity> destinations = iDestinationService.listByIds(destinationIds);
        Map<Long, DestinationEntity> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(destinations)) {
            for (DestinationEntity destination : destinations) {
                map.put(destination.getId(), destination);
            }
        }
        return map;
    }

    private MerchantAdminVo toMerchantAdminVo(MerchantEntity entity,
                                              Map<Long, UserEntity> userMap,
                                              Map<Long, DestinationEntity> destinationMap) {
        MerchantAdminVo vo = new MerchantAdminVo();
        vo.setId(entity.getId());
        vo.setMerchantCode(entity.getMerchantCode());
        vo.setUserId(entity.getUserId());
        vo.setDestinationId(entity.getDestinationId());
        vo.setStoreName(entity.getStoreName());
        vo.setStoreType(entity.getStoreType());
        vo.setStoreTypeLabel(MerchantStoreType.labelOf(entity.getStoreType()));
        vo.setShopCategory(entity.getShopCategory());
        vo.setShopCategoryLabel(resolveShopCategoryLabel(entity.getShopCategory()));
        vo.setContactName(entity.getContactName());
        vo.setContactPhone(entity.getContactPhone());
        vo.setStoreRegion(entity.getStoreRegion());
        vo.setStoreAddress(entity.getStoreAddress());
        vo.setStoreAddressDetail(resolveStoreAddressDetail(entity));
        vo.setLatitude(entity.getLatitude());
        vo.setLongitude(entity.getLongitude());
        vo.setStoreHours(entity.getStoreHours());
        vo.setLicenseNo(entity.getLicenseNo());
        vo.setLicenseImage(entity.getLicenseImage());
        vo.setLegalName(entity.getLegalName());
        vo.setLegalIdNo(entity.getLegalIdNo());
        vo.setLegalIdFront(entity.getLegalIdFront());
        vo.setLegalIdBack(entity.getLegalIdBack());
        vo.setQualImages(parseQualImages(entity.getQualImages()));
        vo.setStatus(entity.getStatus());
        vo.setStatusLabel(resolveMerchantStatusLabel(entity.getStatus()));
        vo.setIsOpen(entity.getIsOpen());
        vo.setIsOpenLabel(resolveMerchantOpenLabel(entity.getIsOpen()));
        vo.setHomeRecommendEnabled(entity.getHomeRecommendEnabled());
        vo.setHomeRecommendEnabledLabel(resolveHomeRecommendLabel(entity.getHomeRecommendEnabled()));
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        UserEntity user = userMap.get(entity.getUserId());
        if (user != null) {
            vo.setUserPhone(user.getPhone());
            vo.setUserName(user.getUserName());
        }
        DestinationEntity destination = destinationMap.get(entity.getDestinationId());
        vo.setDestinationName(DestinationDisplaySupport.resolveLabel(
                destination, entity.getStoreAddress(), entity.getStoreRegion()));
        return vo;
    }

    private String resolveMerchantStatusLabel(Integer status) {
        if (Objects.equals(status, MERCHANT_STATUS_NORMAL)) {
            return "正常";
        }
        if (Objects.equals(status, MERCHANT_STATUS_DISABLED)) {
            return "已禁用";
        }
        return "待激活";
    }

    private String resolveMerchantOpenLabel(Integer isOpen) {
        return Objects.equals(isOpen, MERCHANT_OPEN) ? "营业中" : "休息中";
    }

    private String resolveHomeRecommendLabel(Integer homeRecommendEnabled) {
        return Objects.equals(homeRecommendEnabled, HOME_RECOMMEND_ENABLED) ? "已推荐" : "未推荐";
    }

    private Map<Long, UserEntity> loadUserMapByMerchants(List<MerchantEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> userIds = records.stream()
                .map(MerchantEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<UserEntity> users = iUserService.listByIds(userIds);
        Map<Long, UserEntity> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(users)) {
            for (UserEntity user : users) {
                map.put(user.getUserId(), user);
            }
        }
        return map;
    }

    private Map<Long, DestinationEntity> loadDestinationMapByMerchants(List<MerchantEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> destinationIds = records.stream()
                .map(MerchantEntity::getDestinationId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(destinationIds)) {
            return Collections.emptyMap();
        }
        List<DestinationEntity> destinations = iDestinationService.listByIds(destinationIds);
        Map<Long, DestinationEntity> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(destinations)) {
            for (DestinationEntity destination : destinations) {
                map.put(destination.getId(), destination);
            }
        }
        return map;
    }

    private Map<Long, MerchantEntity> loadMerchantMap(List<MerchantApplyEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> merchantIds = records.stream()
                .map(MerchantApplyEntity::getMerchantId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(merchantIds)) {
            return Collections.emptyMap();
        }
        List<MerchantEntity> merchants = iMerchantService.listByIds(merchantIds);
        Map<Long, MerchantEntity> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(merchants)) {
            for (MerchantEntity merchant : merchants) {
                map.put(merchant.getId(), merchant);
            }
        }
        return map;
    }

    private MerchantEntity findMerchantByUserId(Long userId) {
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantEntity::getUserId, userId)
                .orderByDesc(MerchantEntity::getId)
                .last("LIMIT 1");
        return iMerchantService.getOne(queryWrapper, false);
    }

    private MerchantEntity findActiveMerchantByUserId(Long userId) {
        MerchantEntity merchant = findMerchantByUserId(userId);
        if (merchant != null && Objects.equals(merchant.getStatus(), MERCHANT_STATUS_NORMAL)) {
            return merchant;
        }
        return null;
    }

    private MerchantApplyEntity findPendingApply(Long userId) {
        LambdaQueryWrapper<MerchantApplyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantApplyEntity::getUserId, userId)
                .eq(MerchantApplyEntity::getAuditStatus, AUDIT_PENDING)
                .orderByDesc(MerchantApplyEntity::getId)
                .last("LIMIT 1");
        return iMerchantApplyService.getOne(queryWrapper, false);
    }

    private MerchantApplyEntity findLatestApply(Long userId) {
        LambdaQueryWrapper<MerchantApplyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantApplyEntity::getUserId, userId)
                .orderByDesc(MerchantApplyEntity::getSubmittedAt)
                .orderByDesc(MerchantApplyEntity::getId)
                .last("LIMIT 1");
        return iMerchantApplyService.getOne(queryWrapper, false);
    }

    private UserEntity requireUser(Long userId) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        return user;
    }

    private Long resolveDestinationId(Long destinationId) {
        if (destinationId != null) {
            DestinationEntity destination = iDestinationService.getById(destinationId);
            if (destination != null) {
                return destination.getId();
            }
        }
        LambdaQueryWrapper<DestinationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DestinationEntity::getIsDefault, 1)
                .last("LIMIT 1");
        DestinationEntity defaultDestination = iDestinationService.getOne(queryWrapper, false);
        if (defaultDestination != null) {
            return defaultDestination.getId();
        }
        DestinationEntity first = iDestinationService.getOne(new LambdaQueryWrapper<DestinationEntity>()
                .orderByAsc(DestinationEntity::getSort)
                .orderByAsc(DestinationEntity::getId)
                .last("LIMIT 1"), false);
        if (first == null) {
            throw new BizException(AppError.DESTINATION_NOT_FOUND);
        }
        return first.getId();
    }

    private void incrementDestinationMerchantCount(Long destinationId) {
        if (destinationId == null) {
            return;
        }
        DestinationEntity destination = iDestinationService.getById(destinationId);
        if (destination == null) {
            return;
        }
        int nextCount = destination.getMerchantCount() == null ? 1 : destination.getMerchantCount() + 1;
        LambdaUpdateWrapper<DestinationEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DestinationEntity::getId, destinationId)
                .set(DestinationEntity::getMerchantCount, nextCount);
        iDestinationService.update(new DestinationEntity(), updateWrapper);
    }

    private MerchantEntity buildMerchantEntity(MerchantApplyBo applyBo) {
        MerchantEntity merchant = new MerchantEntity();
        applyMerchantPayload(merchant, applyBo);
        return merchant;
    }

    private MerchantApplyBo toApplyBo(MerchantApplyEntity apply) {
        MerchantApplyBo applyBo = new MerchantApplyBo();
        applyBo.setContactName(apply.getContactName());
        applyBo.setContactPhone(apply.getContactPhone());
        applyBo.setStoreName(apply.getStoreName());
        applyBo.setStoreType(apply.getStoreType());
        applyBo.setShopCategory(apply.getShopCategory());
        applyBo.setStoreRegion(apply.getStoreRegion());
        applyBo.setStoreAddress(apply.getStoreAddress());
        applyBo.setLatitude(apply.getLatitude());
        applyBo.setLongitude(apply.getLongitude());
        applyBo.setStoreHours(apply.getStoreHours());
        applyBo.setLicenseNo(apply.getLicenseNo());
        applyBo.setLicenseImage(apply.getLicenseImage());
        applyBo.setLegalName(apply.getLegalName());
        applyBo.setLegalIdNo(apply.getLegalIdNo());
        applyBo.setLegalIdFront(apply.getLegalIdFront());
        applyBo.setLegalIdBack(apply.getLegalIdBack());
        applyBo.setQualImages(parseQualImages(apply.getQualImages()));

        MerchantEntity addressProbe = new MerchantEntity();
        addressProbe.setStoreRegion(apply.getStoreRegion());
        addressProbe.setStoreAddress(apply.getStoreAddress());
        applyBo.setStoreAddressDetail(resolveStoreAddressDetail(addressProbe));
        return applyBo;
    }

    private void applyMerchantPayload(MerchantEntity merchant, MerchantApplyBo applyBo) {
        merchant.setStoreName(StringUtils.trim(applyBo.getStoreName()));
        merchant.setStoreType(MerchantStoreType.normalize(applyBo.getStoreType()));
        merchant.setShopCategory(normalizeShopCategory(applyBo.getShopCategory()));
        merchant.setCraftId(null);
        merchant.setContactName(StringUtils.trim(applyBo.getContactName()));
        merchant.setContactPhone(StringUtils.trim(applyBo.getContactPhone()));
        merchant.setStoreRegion(StringUtils.trim(applyBo.getStoreRegion()));
        merchant.setStoreAddress(MerchantApplySupport.resolveStoreAddress(applyBo));
        merchant.setStoreHours(StringUtils.defaultIfBlank(StringUtils.trimToNull(applyBo.getStoreHours()), "09:00-21:00"));
        merchant.setLicenseNo(StringUtils.trim(applyBo.getLicenseNo()));
        merchant.setLicenseImage(StringUtils.trim(applyBo.getLicenseImage()));
        merchant.setLegalName(StringUtils.trim(applyBo.getLegalName()));
        merchant.setLegalIdNo(StringUtils.trim(applyBo.getLegalIdNo()));
        merchant.setLegalIdFront(StringUtils.trim(applyBo.getLegalIdFront()));
        merchant.setLegalIdBack(StringUtils.trim(applyBo.getLegalIdBack()));
        merchant.setQualImages(serializeQualImages(applyBo.getQualImages()));
    }

    private void linkPendingApplyIfNeeded(Long userId, Long merchantId) {
        MerchantApplyEntity pending = findPendingApply(userId);
        if (pending == null) {
            return;
        }
        LocalDateTime auditedAt = LocalDateTime.now();
        LambdaUpdateWrapper<MerchantApplyEntity> applyUpdate = new LambdaUpdateWrapper<>();
        applyUpdate.eq(MerchantApplyEntity::getId, pending.getId())
                .set(MerchantApplyEntity::getAuditStatus, AUDIT_APPROVED)
                .set(MerchantApplyEntity::getAuditRemark, "管理端直接创建商户")
                .set(MerchantApplyEntity::getMerchantId, merchantId)
                .set(MerchantApplyEntity::getAuditedAt, auditedAt);
        iMerchantApplyService.update(new MerchantApplyEntity(), applyUpdate);
    }

    private String resolveStoreAddressDetail(MerchantEntity entity) {
        if (entity == null) {
            return null;
        }
        String fullAddress = StringUtils.trimToEmpty(entity.getStoreAddress());
        String region = StringUtils.trimToEmpty(entity.getStoreRegion());
        if (StringUtils.isNotBlank(fullAddress) && StringUtils.isNotBlank(region) && fullAddress.startsWith(region)) {
            return StringUtils.trimToEmpty(fullAddress.substring(region.length()));
        }
        return fullAddress;
    }

    private String resolveStoreAddress(MerchantApplyBo applyBo) {
        return MerchantApplySupport.resolveStoreAddress(applyBo);
    }

    private String serializeQualImages(Map<String, String> qualImages) {
        if (qualImages == null || qualImages.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(qualImages);
    }

    private void validateCultureShopCategory(MerchantApplyBo applyBo) {
        String storeType = MerchantStoreType.normalize(applyBo.getStoreType());
        if (!MerchantStoreType.CULTURE.getCode().equals(storeType)) {
            applyBo.setShopCategory(null);
            applyBo.setCraftId(null);
            return;
        }
        applyBo.setCraftId(null);
        String category = normalizeShopCategory(applyBo.getShopCategory());
        if (StringUtils.isBlank(category)) {
            throw new BizException(AppError.HERITAGE_SHOP_CATEGORY_REQUIRED);
        }
        if (!heritageShopCategoryService.isValidForSave(category)) {
            throw new BizException(AppError.HERITAGE_SHOP_CATEGORY_NOT_FOUND);
        }
        applyBo.setShopCategory(category);
    }

    private void syncCultureMerchantShopCategory(Long merchantId, String storeType) {
        if (merchantId == null || !MerchantStoreType.CULTURE.getCode().equals(MerchantStoreType.normalize(storeType))) {
            return;
        }
        heritageShopService.syncCategoryFromMerchant(merchantId);
    }

    private String normalizeShopCategory(String shopCategory) {
        if (StringUtils.isBlank(shopCategory)) {
            return null;
        }
        return heritageShopCategoryService.normalize(StringUtils.trim(shopCategory));
    }

    private String resolveShopCategoryLabel(String shopCategory) {
        if (StringUtils.isBlank(shopCategory)) {
            return null;
        }
        String label = heritageShopCategoryService.labelOf(shopCategory);
        return StringUtils.isBlank(label) ? shopCategory : label;
    }

    private Map<String, String> parseQualImages(String qualImages) {
        if (StringUtils.isBlank(qualImages)) {
            return Collections.emptyMap();
        }
        try {
            return JSON.parseObject(qualImages, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private String generateApplyNo() {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "MA" + LocalDateTime.now().format(APPLY_NO_FORMAT) + suffix;
    }

    private String generateMerchantCode() {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "MC" + LocalDateTime.now().format(APPLY_NO_FORMAT) + suffix;
    }
}

package com.sqx.app.lvyou.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.service.MerchantWalletService;
import com.sqx.app.lvyou.wallet.db.service.IWithdrawRecordService;
import com.sqx.app.lvyou.wallet.domain.bo.WithdrawAdminAuditBo;
import com.sqx.app.lvyou.wallet.domain.bo.WithdrawAdminPageBo;
import com.sqx.app.lvyou.wallet.domain.entity.WithdrawRecordEntity;
import com.sqx.app.lvyou.wallet.domain.vo.WithdrawAdminVo;
import com.sqx.app.lvyou.wallet.enums.WithdrawOwnerType;
import com.sqx.app.lvyou.wallet.enums.WithdrawStatus;
import com.sqx.app.lvyou.wallet.service.WalletService;
import com.sqx.app.lvyou.wallet.service.WithdrawAdminService;
import com.sqx.app.lvyou.wallet.support.WalletSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理端提现审核服务实现
 */
@Service
public class WithdrawAdminServiceImpl implements WithdrawAdminService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private IWithdrawRecordService iWithdrawRecordService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private WalletService walletService;

    @Resource
    private MerchantWalletService merchantWalletService;

    @Override
    public IPage<WithdrawAdminVo> page(WithdrawAdminPageBo pageBo) {
        Page<WithdrawRecordEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<WithdrawRecordEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (Objects.nonNull(pageBo.getId())) {
            queryWrapper.eq(WithdrawRecordEntity::getId, pageBo.getId());
        }
        if (StringUtils.isNotBlank(pageBo.getWithdrawNo())) {
            queryWrapper.like(WithdrawRecordEntity::getWithdrawNo, pageBo.getWithdrawNo().trim());
        }
        String ownerType = WithdrawOwnerType.normalize(pageBo.getOwnerType());
        if (ownerType != null) {
            queryWrapper.eq(WithdrawRecordEntity::getOwnerType, ownerType);
        }
        String status = WithdrawStatus.normalize(pageBo.getStatus());
        if (status != null) {
            queryWrapper.eq(WithdrawRecordEntity::getStatus, status);
        }
        if (StringUtils.isNotBlank(pageBo.getMethod())) {
            queryWrapper.eq(WithdrawRecordEntity::getMethod, StringUtils.lowerCase(pageBo.getMethod().trim()));
        }

        List<Long> userIds = resolveUserIdsByPhone(pageBo.getUserPhone());
        if (userIds != null) {
            if (userIds.isEmpty()) {
                return emptyPage(page);
            }
            queryWrapper.in(WithdrawRecordEntity::getUserId, userIds);
        }

        List<Long> merchantIds = resolveMerchantIdsByName(pageBo.getMerchantName());
        if (merchantIds != null) {
            if (merchantIds.isEmpty()) {
                return emptyPage(page);
            }
            queryWrapper.in(WithdrawRecordEntity::getMerchantId, merchantIds);
        }

        queryWrapper.orderByDesc(WithdrawRecordEntity::getCreateTime)
                .orderByDesc(WithdrawRecordEntity::getId);

        IPage<WithdrawRecordEntity> entityPage = iWithdrawRecordService.page(page, queryWrapper);
        Map<Long, UserEntity> userMap = loadUserMap(entityPage.getRecords());
        Map<Long, MerchantEntity> merchantMap = loadMerchantMap(entityPage.getRecords());

        List<WithdrawAdminVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (WithdrawRecordEntity record : entityPage.getRecords()) {
                voList.add(toAdminVo(record, userMap, merchantMap));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(WithdrawAdminAuditBo auditBo) {
        WithdrawRecordEntity record = iWithdrawRecordService.getById(auditBo.getId());
        if (record == null) {
            throw new BizException(AppError.WALLET_WITHDRAW_NOT_FOUND);
        }
        if (!WithdrawStatus.PENDING.equals(record.getStatus())) {
            throw new BizException(AppError.WALLET_WITHDRAW_ALREADY_AUDITED);
        }

        String action = StringUtils.lowerCase(StringUtils.trimToEmpty(auditBo.getAction()));
        if ("pass".equals(action)) {
            approve(record);
            return;
        }
        if ("reject".equals(action)) {
            reject(record, auditBo.getReason());
            return;
        }
        throw new BizException(AppError.WALLET_WITHDRAW_AUDIT_ACTION_INVALID);
    }

    private void approve(WithdrawRecordEntity record) {
        LambdaUpdateWrapper<WithdrawRecordEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WithdrawRecordEntity::getId, record.getId())
                .eq(WithdrawRecordEntity::getStatus, WithdrawStatus.PENDING)
                .set(WithdrawRecordEntity::getStatus, WithdrawStatus.SUCCESS)
                .set(WithdrawRecordEntity::getRemark, "已到账")
                .set(WithdrawRecordEntity::getAuditTime, java.time.LocalDateTime.now());
        if (!iWithdrawRecordService.update(updateWrapper)) {
            throw new BizException(AppError.WALLET_WITHDRAW_ALREADY_AUDITED);
        }

        if (WithdrawOwnerType.USER.equals(record.getOwnerType())) {
            walletService.confirmWithdrawSuccess(record.getUserId(), record.getAmount(), record.getWithdrawNo());
            return;
        }
        if (WithdrawOwnerType.MERCHANT.equals(record.getOwnerType())) {
            merchantWalletService.confirmWithdrawSuccess(record.getMerchantId(), record.getAmount(), record.getWithdrawNo());
        }
    }

    private void reject(WithdrawRecordEntity record, String reason) {
        if (StringUtils.isBlank(reason)) {
            throw new BizException(AppError.WALLET_WITHDRAW_REJECT_REASON_REQUIRED);
        }

        LambdaUpdateWrapper<WithdrawRecordEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WithdrawRecordEntity::getId, record.getId())
                .eq(WithdrawRecordEntity::getStatus, WithdrawStatus.PENDING)
                .set(WithdrawRecordEntity::getStatus, WithdrawStatus.FAILED)
                .set(WithdrawRecordEntity::getRemark, StringUtils.trim(reason))
                .set(WithdrawRecordEntity::getAuditTime, java.time.LocalDateTime.now());
        if (!iWithdrawRecordService.update(updateWrapper)) {
            throw new BizException(AppError.WALLET_WITHDRAW_ALREADY_AUDITED);
        }

        BigDecimal amount = WalletSupport.scaleAmount(record.getAmount());
        if (WithdrawOwnerType.USER.equals(record.getOwnerType())) {
            walletService.refundBalance(record.getUserId(), amount, record.getWithdrawNo(), "提现驳回退款");
            walletService.updateTransactionStatus(record.getUserId(), record.getWithdrawNo(), WithdrawStatus.FAILED);
            return;
        }
        if (WithdrawOwnerType.MERCHANT.equals(record.getOwnerType())) {
            merchantWalletService.refundBalance(record.getMerchantId(), amount, record.getWithdrawNo(), "提现驳回退款");
            merchantWalletService.updateTransactionStatus(record.getMerchantId(), record.getWithdrawNo(), WithdrawStatus.FAILED);
        }
    }

    private List<Long> resolveUserIdsByPhone(String userPhone) {
        if (StringUtils.isBlank(userPhone)) {
            return null;
        }
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(UserEntity::getPhone, userPhone.trim());
        List<UserEntity> users = iUserService.list(queryWrapper);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        return users.stream().map(UserEntity::getUserId).collect(Collectors.toList());
    }

    private List<Long> resolveMerchantIdsByName(String merchantName) {
        if (StringUtils.isBlank(merchantName)) {
            return null;
        }
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(MerchantEntity::getStoreName, merchantName.trim());
        List<MerchantEntity> merchants = iMerchantService.list(queryWrapper);
        if (CollectionUtils.isEmpty(merchants)) {
            return Collections.emptyList();
        }
        return merchants.stream().map(MerchantEntity::getId).collect(Collectors.toList());
    }

    private Map<Long, UserEntity> loadUserMap(List<WithdrawRecordEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        Set<Long> userIds = records.stream()
                .map(WithdrawRecordEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
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

    private Map<Long, MerchantEntity> loadMerchantMap(List<WithdrawRecordEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        Set<Long> merchantIds = records.stream()
                .map(WithdrawRecordEntity::getMerchantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (merchantIds.isEmpty()) {
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

    private WithdrawAdminVo toAdminVo(WithdrawRecordEntity record, Map<Long, UserEntity> userMap,
                                      Map<Long, MerchantEntity> merchantMap) {
        WithdrawAdminVo vo = new WithdrawAdminVo();
        vo.setId(record.getId());
        vo.setWithdrawNo(record.getWithdrawNo());
        vo.setOwnerType(record.getOwnerType());
        vo.setUserId(record.getUserId());
        vo.setMerchantId(record.getMerchantId());
        vo.setAmount(record.getAmount());
        vo.setFeeAmount(record.getFeeAmount());
        vo.setActualAmount(record.getActualAmount() != null ? record.getActualAmount() : record.getAmount());
        vo.setMethod(record.getMethod());
        vo.setAccount(record.getAccount());
        vo.setRealName(record.getRealName());
        vo.setQrCodeUrl(record.getQrCodeUrl());
        vo.setStatus(record.getStatus());
        vo.setRemark(record.getRemark());
        vo.setCreateTime(record.getCreateTime() == null ? "" : record.getCreateTime().format(TIME_FORMATTER));
        vo.setAuditTime(record.getAuditTime() == null ? "" : record.getAuditTime().format(TIME_FORMATTER));

        UserEntity user = userMap.get(record.getUserId());
        if (user != null) {
            vo.setUserPhone(user.getPhone());
            vo.setUserName(user.getUserName());
        }
        MerchantEntity merchant = merchantMap.get(record.getMerchantId());
        if (merchant != null) {
            vo.setMerchantName(merchant.getStoreName());
        }
        return vo;
    }

    private IPage<WithdrawAdminVo> emptyPage(Page<WithdrawRecordEntity> page) {
        IPage<WithdrawAdminVo> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(0);
        result.setPages(0);
        result.setRecords(Collections.emptyList());
        return result;
    }
}

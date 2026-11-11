package com.sqx.app.base.user.cancel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.cancel.db.service.ICancelAccountService;
import com.sqx.app.base.user.cancel.domain.bo.CancelAccountAuditBo;
import com.sqx.app.base.user.cancel.domain.bo.CancelAccountPageBo;
import com.sqx.app.base.user.cancel.domain.bo.CancelAccountSubmitBo;
import com.sqx.app.base.user.cancel.domain.entity.CancelAccountEntity;
import com.sqx.app.base.user.cancel.domain.vo.CancelAccountSubmitVo;
import com.sqx.app.base.user.cancel.domain.vo.CancelAccountVo;
import com.sqx.app.base.user.cancel.enums.CancelAccountStatus;
import com.sqx.app.base.user.cancel.service.CancelAccountService;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.enums.UserStatus;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CancelAccountServiceImpl implements CancelAccountService {

    @Resource
    private ICancelAccountService iCancelAccountService;

    @Resource
    private IUserService iUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CancelAccountSubmitVo submit(Long userId, CancelAccountSubmitBo submitBo) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        if (!UserStatus.ENABLE.getCode().equals(user.getStatus())) {
            throw new BizException(AppError.ACCOUNT_ABNORMAL);
        }

        long pendingCount = iCancelAccountService.count(new LambdaQueryWrapper<CancelAccountEntity>()
                .eq(CancelAccountEntity::getUserId, userId)
                .eq(CancelAccountEntity::getStatus, CancelAccountStatus.PENDING.getCode()));
        if (pendingCount > 0) {
            throw new BizException(AppError.CANCEL_ACCOUNT_PENDING_EXISTS);
        }

        CancelAccountEntity entity = new CancelAccountEntity();
        entity.setUserId(userId);
        entity.setPhone(user.getPhone());
        entity.setUserName(user.getUserName());
        entity.setReason(StringUtils.trimToNull(submitBo.getReason()));
        entity.setStatus(CancelAccountStatus.PENDING.getCode());
        iCancelAccountService.save(entity);

        CancelAccountSubmitVo vo = new CancelAccountSubmitVo();
        vo.setId(entity.getId());
        vo.setStatus(entity.getStatus());
        vo.setStatusLabel(CancelAccountStatus.labelOf(entity.getStatus()));
        return vo;
    }

    @Override
    public IPage<CancelAccountVo> page(CancelAccountPageBo pageBo) {
        Page<CancelAccountEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<CancelAccountEntity> queryWrapper = new LambdaQueryWrapper<>();

        String status = CancelAccountStatus.normalize(pageBo.getStatus());
        if (status != null) {
            queryWrapper.eq(CancelAccountEntity::getStatus, status);
        }
        if (StringUtils.isNotBlank(pageBo.getUserPhone())) {
            queryWrapper.like(CancelAccountEntity::getPhone, pageBo.getUserPhone().trim());
        }
        if (StringUtils.isNotBlank(pageBo.getUserName())) {
            queryWrapper.like(CancelAccountEntity::getUserName, pageBo.getUserName().trim());
        }
        queryWrapper.orderByDesc(CancelAccountEntity::getCreateTime)
                .orderByDesc(CancelAccountEntity::getId);

        IPage<CancelAccountEntity> entityPage = iCancelAccountService.page(page, queryWrapper);
        List<CancelAccountVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (CancelAccountEntity record : entityPage.getRecords()) {
                voList.add(toVo(record));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(CancelAccountAuditBo auditBo) {
        CancelAccountEntity record = iCancelAccountService.getById(auditBo.getId());
        if (record == null) {
            throw new BizException(AppError.CANCEL_ACCOUNT_NOT_FOUND);
        }
        if (!CancelAccountStatus.PENDING.getCode().equals(record.getStatus())) {
            throw new BizException(AppError.CANCEL_ACCOUNT_ALREADY_AUDITED);
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
        throw new BizException(AppError.CANCEL_ACCOUNT_AUDIT_ACTION_INVALID);
    }

    private void approve(CancelAccountEntity record) {
        UserEntity user = iUserService.getById(record.getUserId());
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<CancelAccountEntity> recordUpdate = new LambdaUpdateWrapper<>();
        recordUpdate.eq(CancelAccountEntity::getId, record.getId())
                .eq(CancelAccountEntity::getStatus, CancelAccountStatus.PENDING.getCode())
                .set(CancelAccountEntity::getStatus, CancelAccountStatus.APPROVED.getCode())
                .set(CancelAccountEntity::getAuditRemark, "已通过")
                .set(CancelAccountEntity::getAuditTime, now);
        if (!iCancelAccountService.update(recordUpdate)) {
            throw new BizException(AppError.CANCEL_ACCOUNT_ALREADY_AUDITED);
        }

        String deletedPhone = "del_" + user.getUserId() + "_" + System.currentTimeMillis();
        LambdaUpdateWrapper<UserEntity> userUpdate = new LambdaUpdateWrapper<>();
        userUpdate.eq(UserEntity::getUserId, user.getUserId())
                .set(UserEntity::getStatus, UserStatus.CANCELLED.getCode())
                .set(UserEntity::getPhone, deletedPhone)
                .set(UserEntity::getMaOpenId, null)
                .set(UserEntity::getMpOpenId, null)
                .set(UserEntity::getAppleId, null)
                .set(UserEntity::getPassword, null)
                .set(UserEntity::getCid, null);
        iUserService.update(userUpdate);
    }

    private void reject(CancelAccountEntity record, String reason) {
        if (StringUtils.isBlank(reason)) {
            throw new BizException(AppError.CANCEL_ACCOUNT_REJECT_REASON_REQUIRED);
        }

        LambdaUpdateWrapper<CancelAccountEntity> recordUpdate = new LambdaUpdateWrapper<>();
        recordUpdate.eq(CancelAccountEntity::getId, record.getId())
                .eq(CancelAccountEntity::getStatus, CancelAccountStatus.PENDING.getCode())
                .set(CancelAccountEntity::getStatus, CancelAccountStatus.REJECTED.getCode())
                .set(CancelAccountEntity::getAuditRemark, StringUtils.trim(reason))
                .set(CancelAccountEntity::getAuditTime, LocalDateTime.now());
        if (!iCancelAccountService.update(recordUpdate)) {
            throw new BizException(AppError.CANCEL_ACCOUNT_ALREADY_AUDITED);
        }
    }

    private CancelAccountVo toVo(CancelAccountEntity entity) {
        CancelAccountVo vo = new CancelAccountVo();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setPhone(entity.getPhone());
        vo.setUserName(entity.getUserName());
        vo.setReason(entity.getReason());
        vo.setStatus(entity.getStatus());
        vo.setStatusLabel(CancelAccountStatus.labelOf(entity.getStatus()));
        vo.setAuditRemark(entity.getAuditRemark());
        vo.setAuditTime(entity.getAuditTime());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}

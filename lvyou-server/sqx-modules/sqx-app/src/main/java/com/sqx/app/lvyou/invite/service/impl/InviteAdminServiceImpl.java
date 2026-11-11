package com.sqx.app.lvyou.invite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.invite.db.service.ICommissionRecordService;
import com.sqx.app.lvyou.invite.db.service.IInviteRelationService;
import com.sqx.app.lvyou.invite.domain.bo.CommissionRecordAdminPageBo;
import com.sqx.app.lvyou.invite.domain.bo.InviteRelationAdminPageBo;
import com.sqx.app.lvyou.invite.domain.entity.CommissionRecordEntity;
import com.sqx.app.lvyou.invite.domain.entity.InviteRelationEntity;
import com.sqx.app.lvyou.invite.domain.vo.CommissionRecordAdminVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteRelationAdminVo;
import com.sqx.app.lvyou.invite.service.InviteAdminService;
import com.sqx.app.lvyou.invite.support.InviteSupport;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理端邀请分销服务实现
 */
@Service
public class InviteAdminServiceImpl implements InviteAdminService {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private IInviteRelationService iInviteRelationService;

    @Resource
    private ICommissionRecordService iCommissionRecordService;

    @Resource
    private IUserService iUserService;

    @Override
    public IPage<InviteRelationAdminVo> relationPage(InviteRelationAdminPageBo pageBo) {
        Page<InviteRelationEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<InviteRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageBo.getInviteCode()),
                        InviteRelationEntity::getInviteCode, pageBo.getInviteCode())
                .orderByDesc(InviteRelationEntity::getCreateTime)
                .orderByDesc(InviteRelationEntity::getId);

        Set<Long> memberFilter = resolveUserIdFilter(pageBo.getMemberPhone());
        if (memberFilter != null) {
            if (memberFilter.isEmpty()) {
                return PageUtil.copyPage(page, Collections.emptyList());
            }
            queryWrapper.in(InviteRelationEntity::getUserId, memberFilter);
        }

        Set<Long> inviterFilter = resolveUserIdFilter(pageBo.getInviterPhone());
        if (inviterFilter != null) {
            if (inviterFilter.isEmpty()) {
                return PageUtil.copyPage(page, Collections.emptyList());
            }
            queryWrapper.in(InviteRelationEntity::getInviterUserId, inviterFilter);
        }

        Set<Long> inviterLevel2Filter = resolveUserIdFilter(pageBo.getInviterLevel2Phone());
        if (inviterLevel2Filter != null) {
            if (inviterLevel2Filter.isEmpty()) {
                return PageUtil.copyPage(page, Collections.emptyList());
            }
            queryWrapper.in(InviteRelationEntity::getInviterLevel2UserId, inviterLevel2Filter);
        }

        IPage<InviteRelationEntity> entityPage = iInviteRelationService.page(page, queryWrapper);
        return PageUtil.copyPage(entityPage, toRelationVoList(entityPage.getRecords()));
    }

    @Override
    public IPage<CommissionRecordAdminVo> commissionPage(CommissionRecordAdminPageBo pageBo) {
        Page<CommissionRecordEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<CommissionRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(pageBo.getId()), CommissionRecordEntity::getId, pageBo.getId())
                .like(StringUtils.isNotBlank(pageBo.getOrderNo()),
                        CommissionRecordEntity::getOrderNo, pageBo.getOrderNo())
                .eq(pageBo.getCommissionLevel() != null,
                        CommissionRecordEntity::getCommissionLevel, pageBo.getCommissionLevel())
                .eq(StringUtils.isNotBlank(pageBo.getStatus()),
                        CommissionRecordEntity::getStatus, pageBo.getStatus())
                .orderByDesc(CommissionRecordEntity::getCreateTime)
                .orderByDesc(CommissionRecordEntity::getId);

        Set<Long> beneficiaryFilter = resolveUserIdFilter(pageBo.getBeneficiaryPhone());
        if (beneficiaryFilter != null) {
            if (beneficiaryFilter.isEmpty()) {
                return PageUtil.copyPage(page, Collections.emptyList());
            }
            queryWrapper.in(CommissionRecordEntity::getBeneficiaryUserId, beneficiaryFilter);
        }

        Set<Long> sourceFilter = resolveUserIdFilter(pageBo.getSourcePhone());
        if (sourceFilter != null) {
            if (sourceFilter.isEmpty()) {
                return PageUtil.copyPage(page, Collections.emptyList());
            }
            queryWrapper.in(CommissionRecordEntity::getSourceUserId, sourceFilter);
        }

        IPage<CommissionRecordEntity> entityPage = iCommissionRecordService.page(page, queryWrapper);
        return PageUtil.copyPage(entityPage, toCommissionVoList(entityPage.getRecords()));
    }

    private List<InviteRelationAdminVo> toRelationVoList(List<InviteRelationEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        Set<Long> userIds = new HashSet<>();
        for (InviteRelationEntity record : records) {
            if (record.getUserId() != null) {
                userIds.add(record.getUserId());
            }
            if (record.getInviterUserId() != null) {
                userIds.add(record.getInviterUserId());
            }
            if (record.getInviterLevel2UserId() != null) {
                userIds.add(record.getInviterLevel2UserId());
            }
        }
        Map<Long, UserEntity> userMap = loadUserMap(userIds);

        List<InviteRelationAdminVo> voList = new ArrayList<>();
        for (InviteRelationEntity record : records) {
            InviteRelationAdminVo vo = new InviteRelationAdminVo();
            vo.setId(record.getId());
            vo.setInviteCode(StringUtils.defaultString(record.getInviteCode()));
            vo.setBindTime(record.getCreateTime() == null ? null : record.getCreateTime().format(DATETIME_FMT));
            fillUserFields(vo, userMap.get(record.getUserId()));
            fillInviterFields(vo, userMap.get(record.getInviterUserId()));
            fillInviterLevel2Fields(vo, userMap.get(record.getInviterLevel2UserId()));
            voList.add(vo);
        }
        return voList;
    }

    private List<CommissionRecordAdminVo> toCommissionVoList(List<CommissionRecordEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        Set<Long> userIds = new HashSet<>();
        for (CommissionRecordEntity record : records) {
            if (record.getBeneficiaryUserId() != null) {
                userIds.add(record.getBeneficiaryUserId());
            }
            if (record.getSourceUserId() != null) {
                userIds.add(record.getSourceUserId());
            }
        }
        Map<Long, UserEntity> userMap = loadUserMap(userIds);

        List<CommissionRecordAdminVo> voList = new ArrayList<>();
        for (CommissionRecordEntity record : records) {
            CommissionRecordAdminVo vo = new CommissionRecordAdminVo();
            vo.setId(record.getId());
            vo.setOrderNo(StringUtils.defaultString(record.getOrderNo()));
            vo.setCommissionLevel(record.getCommissionLevel());
            vo.setCommissionLevelLabel(resolveCommissionLevelLabel(record.getCommissionLevel()));
            vo.setOrderAmount(InviteSupport.formatMoney(record.getOrderAmount()));
            vo.setCommissionRate(record.getCommissionRate() == null ? null : record.getCommissionRate().toPlainString());
            vo.setCommissionAmount(InviteSupport.formatMoney(record.getCommissionAmount()));
            vo.setStatus(StringUtils.defaultString(record.getStatus()));
            vo.setStatusLabel(resolveStatusLabel(record.getStatus()));
            vo.setSettleTime(record.getCreateTime() == null ? null : record.getCreateTime().format(DATETIME_FMT));

            UserEntity beneficiary = userMap.get(record.getBeneficiaryUserId());
            if (beneficiary != null) {
                vo.setBeneficiaryName(displayName(beneficiary));
                vo.setBeneficiaryPhone(StringUtils.defaultString(beneficiary.getPhone()));
            }
            UserEntity source = userMap.get(record.getSourceUserId());
            if (source != null) {
                vo.setSourceName(displayName(source));
                vo.setSourcePhone(StringUtils.defaultString(source.getPhone()));
            }
            voList.add(vo);
        }
        return voList;
    }

    private void fillUserFields(InviteRelationAdminVo vo, UserEntity user) {
        if (user == null) {
            return;
        }
        vo.setMemberName(displayName(user));
        vo.setMemberPhone(StringUtils.defaultString(user.getPhone()));
        vo.setMemberInviteCode(StringUtils.defaultString(user.getInvitationCode()));
    }

    private void fillInviterFields(InviteRelationAdminVo vo, UserEntity user) {
        if (user == null) {
            return;
        }
        vo.setInviterName(displayName(user));
        vo.setInviterPhone(StringUtils.defaultString(user.getPhone()));
        vo.setInviterInviteCode(StringUtils.defaultString(user.getInvitationCode()));
    }

    private void fillInviterLevel2Fields(InviteRelationAdminVo vo, UserEntity user) {
        if (user == null) {
            return;
        }
        vo.setInviterLevel2Name(displayName(user));
        vo.setInviterLevel2Phone(StringUtils.defaultString(user.getPhone()));
        vo.setInviterLevel2InviteCode(StringUtils.defaultString(user.getInvitationCode()));
    }

    private String displayName(UserEntity user) {
        return StringUtils.defaultIfBlank(user.getUserName(), InviteSupport.maskPhone(user.getPhone()));
    }

    private String resolveCommissionLevelLabel(Integer level) {
        if (level == null) {
            return "—";
        }
        if (InviteSupport.LEVEL1 == level) {
            return "一级";
        }
        if (InviteSupport.LEVEL2 == level) {
            return "二级";
        }
        return String.valueOf(level);
    }

    private String resolveStatusLabel(String status) {
        if (InviteSupport.COMMISSION_STATUS_SETTLED.equals(status)) {
            return "已结算";
        }
        return StringUtils.defaultIfBlank(status, "—");
    }

    private Map<Long, UserEntity> loadUserMap(Set<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<UserEntity> users = iUserService.listByIds(userIds);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyMap();
        }
        Map<Long, UserEntity> userMap = new HashMap<>();
        for (UserEntity user : users) {
            if (user.getUserId() != null) {
                userMap.put(user.getUserId(), user);
            }
        }
        return userMap;
    }

    private Set<Long> resolveUserIdFilter(String userPhone) {
        if (StringUtils.isBlank(userPhone)) {
            return null;
        }
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(UserEntity::getPhone, userPhone.trim())
                .select(UserEntity::getUserId);
        List<UserEntity> users = iUserService.list(queryWrapper);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptySet();
        }
        return users.stream()
                .map(UserEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}

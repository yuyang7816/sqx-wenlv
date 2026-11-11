package com.sqx.app.lvyou.invite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.appConfig.domain.vo.InviteConfigVO;
import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.utils.AvatarSupport;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.app.lvyou.invite.db.service.ICommissionRecordService;
import com.sqx.app.lvyou.invite.db.service.IInviteRelationService;
import com.sqx.app.lvyou.invite.domain.entity.CommissionRecordEntity;
import com.sqx.app.lvyou.invite.domain.entity.InviteRelationEntity;
import com.sqx.app.lvyou.invite.domain.vo.InviteCommissionDetailVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteCommissionSummaryVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteSummaryVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteTeamMemberVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteTeamSummaryVo;
import com.sqx.app.lvyou.invite.service.InviteService;
import com.sqx.app.lvyou.invite.support.InviteSupport;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.wallet.service.WalletService;
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
import java.util.stream.Collectors;

/**
 * 邀请分销业务服务实现
 */
@Service
public class InviteServiceImpl implements InviteService {

    private static final int MAX_PAGE_LIMIT = 50;
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Resource
    private IInviteRelationService iInviteRelationService;

    @Resource
    private ICommissionRecordService iCommissionRecordService;

    @Resource
    private IUserService iUserService;

    @Resource
    private WalletService walletService;

    @Override
    public InviteConfigVO getConfig() {
        InviteConfigVO vo = new InviteConfigVO();
        vo.setPosterImage(getOptionalConfigValue(SysConfigConstant.Invite.INVITE_POSTER));
        vo.setCopyText(getOptionalConfigValue(SysConfigConstant.Invite.INVITE_COPY_TEXT));
        vo.setRebateLevel1(getOptionalConfigValue(SysConfigConstant.Invite.INVITE_REBATE_LEVEL1));
        vo.setRebateLevel2(getOptionalConfigValue(SysConfigConstant.Invite.INVITE_REBATE_LEVEL2));
        vo.setShareLink(getOptionalConfigValue(SysConfigConstant.Invite.INVITE_SHARE_LINK));
        return vo;
    }

    @Override
    public InviteSummaryVo getSummary(Long userId) {
        UserEntity user = requireUser(userId);
        InviteTeamSummaryVo teamSummary = buildTeamSummary(userId);

        InviteSummaryVo vo = new InviteSummaryVo();
        vo.setAvatar(AvatarSupport.resolve(user.getAvatar()));
        vo.setNickname(StringUtils.defaultIfBlank(user.getUserName(), InviteSupport.maskPhone(user.getPhone())));
        vo.setPhone(InviteSupport.maskPhone(user.getPhone()));
        vo.setInviteCode(StringUtils.defaultString(user.getInvitationCode()));
        vo.setQrContent(InviteSupport.buildQrContent(getOptionalConfigValue(SysConfigConstant.Invite.INVITE_SHARE_LINK),
                user.getInvitationCode()));
        vo.setInviteCount(teamSummary.getInviteCount());
        vo.setTotalEarnings(teamSummary.getTotalEarnings());
        return vo;
    }

    @Override
    public InviteTeamSummaryVo getTeamSummary(Long userId) {
        return buildTeamSummary(userId);
    }

    @Override
    public IPage<InviteTeamMemberVo> getTeamMembers(Long userId, Integer level, Long page, Long limit) {
        int normalizedLevel = normalizeLevel(level);
        Page<InviteRelationEntity> pageParam = initBoundedPage(page, limit);

        LambdaQueryWrapper<InviteRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (normalizedLevel == InviteSupport.LEVEL2) {
            queryWrapper.eq(InviteRelationEntity::getInviterLevel2UserId, userId);
        } else {
            queryWrapper.eq(InviteRelationEntity::getInviterUserId, userId);
        }
        queryWrapper.orderByDesc(InviteRelationEntity::getCreateTime)
                .orderByDesc(InviteRelationEntity::getId);

        IPage<InviteRelationEntity> entityPage = iInviteRelationService.page(pageParam, queryWrapper);
        List<Long> memberUserIds = entityPage.getRecords().stream()
                .map(InviteRelationEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, UserEntity> userMap = loadUserMap(memberUserIds);

        List<InviteTeamMemberVo> voList = new ArrayList<>();
        for (InviteRelationEntity relation : entityPage.getRecords()) {
            UserEntity member = userMap.get(relation.getUserId());
            InviteTeamMemberVo vo = new InviteTeamMemberVo();
            vo.setId(relation.getUserId());
            if (member != null) {
                vo.setAvatar(AvatarSupport.resolve(member.getAvatar()));
                vo.setNickname(StringUtils.defaultIfBlank(member.getUserName(), InviteSupport.maskPhone(member.getPhone())));
            } else {
                vo.setAvatar(AvatarSupport.getDefaultAvatarUrl());
                vo.setNickname("用户" + relation.getUserId());
            }
            vo.setJoinTime(relation.getCreateTime() == null ? null : relation.getCreateTime().format(DATETIME_FMT));
            voList.add(vo);
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public InviteCommissionSummaryVo getCommissionSummary(Long userId) {
        InviteCommissionSummaryVo vo = new InviteCommissionSummaryVo();

        BigDecimal level1Amount = sumCommission(userId, InviteSupport.LEVEL1);
        BigDecimal level2Amount = sumCommission(userId, InviteSupport.LEVEL2);
        long level1Count = countCommission(userId, InviteSupport.LEVEL1);
        long level2Count = countCommission(userId, InviteSupport.LEVEL2);

        vo.setLevel1(InviteSupport.formatMoney(level1Amount));
        vo.setLevel2(InviteSupport.formatMoney(level2Amount));
        vo.setTotal(InviteSupport.formatMoney(level1Amount.add(level2Amount)));
        vo.setLevel1Count(Math.toIntExact(level1Count));
        vo.setLevel2Count(Math.toIntExact(level2Count));
        return vo;
    }

    @Override
    public IPage<InviteCommissionDetailVo> getCommissionDetails(Long userId, Integer level, Long page, Long limit) {
        int normalizedLevel = normalizeLevel(level);
        Page<CommissionRecordEntity> pageParam = initBoundedPage(page, limit);

        LambdaQueryWrapper<CommissionRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommissionRecordEntity::getBeneficiaryUserId, userId)
                .eq(CommissionRecordEntity::getCommissionLevel, normalizedLevel)
                .orderByDesc(CommissionRecordEntity::getCreateTime)
                .orderByDesc(CommissionRecordEntity::getId);

        IPage<CommissionRecordEntity> entityPage = iCommissionRecordService.page(pageParam, queryWrapper);
        List<Long> sourceUserIds = entityPage.getRecords().stream()
                .map(CommissionRecordEntity::getSourceUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserEntity> userMap = loadUserMap(sourceUserIds);

        List<InviteCommissionDetailVo> voList = new ArrayList<>();
        for (CommissionRecordEntity record : entityPage.getRecords()) {
            UserEntity sourceUser = userMap.get(record.getSourceUserId());
            InviteCommissionDetailVo vo = new InviteCommissionDetailVo();
            vo.setId(record.getId());
            vo.setReward(InviteSupport.formatMoney(record.getCommissionAmount()));
            vo.setTime(record.getCreateTime() == null ? null : record.getCreateTime().format(DATETIME_FMT));
            if (sourceUser != null) {
                vo.setAvatar(AvatarSupport.resolve(sourceUser.getAvatar()));
                vo.setNickname(StringUtils.defaultIfBlank(sourceUser.getUserName(),
                        InviteSupport.maskPhone(sourceUser.getPhone())));
            } else {
                vo.setAvatar(AvatarSupport.getDefaultAvatarUrl());
                vo.setNickname("用户" + record.getSourceUserId());
            }
            voList.add(vo);
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindRelationOnRegister(Long userId, String inviteCode) {
        if (userId == null || StringUtils.isBlank(inviteCode)) {
            return;
        }
        LambdaQueryWrapper<InviteRelationEntity> existsWrapper = new LambdaQueryWrapper<>();
        existsWrapper.eq(InviteRelationEntity::getUserId, userId).last("LIMIT 1");
        if (iInviteRelationService.count(existsWrapper) > 0) {
            return;
        }

        String normalizedCode = inviteCode.trim();
        LambdaQueryWrapper<UserEntity> inviterQuery = new LambdaQueryWrapper<>();
        inviterQuery.eq(UserEntity::getInvitationCode, normalizedCode).last("LIMIT 1");
        UserEntity inviter = iUserService.getOne(inviterQuery, false);
        if (inviter == null || Objects.equals(inviter.getUserId(), userId)) {
            return;
        }

        Long level2InviterId = null;
        LambdaQueryWrapper<InviteRelationEntity> inviterRelationQuery = new LambdaQueryWrapper<>();
        inviterRelationQuery.eq(InviteRelationEntity::getUserId, inviter.getUserId()).last("LIMIT 1");
        InviteRelationEntity inviterRelation = iInviteRelationService.getOne(inviterRelationQuery, false);
        if (inviterRelation != null) {
            level2InviterId = inviterRelation.getInviterUserId();
        }

        InviteRelationEntity relation = new InviteRelationEntity();
        relation.setUserId(userId);
        relation.setInviterUserId(inviter.getUserId());
        relation.setInviterLevel2UserId(level2InviterId);
        relation.setInviteCode(normalizedCode);
        iInviteRelationService.save(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleCommissionOnOrderCompleted(OrderEntity order) {
        if (order == null || order.getId() == null || order.getUserId() == null) {
            return;
        }
        if (order.getStatus() == null || order.getStatus() != MerchantOrderStatusGroup.COMPLETED) {
            return;
        }
        BigDecimal payAmount = order.getPayAmount() == null ? BigDecimal.ZERO : order.getPayAmount();
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        LambdaQueryWrapper<InviteRelationEntity> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.eq(InviteRelationEntity::getUserId, order.getUserId()).last("LIMIT 1");
        InviteRelationEntity relation = iInviteRelationService.getOne(relationQuery, false);
        if (relation == null) {
            return;
        }

        settleLevelCommission(order, relation.getInviterUserId(), InviteSupport.LEVEL1);
        settleLevelCommission(order, relation.getInviterLevel2UserId(), InviteSupport.LEVEL2);
    }

    private void settleLevelCommission(OrderEntity order, Long beneficiaryUserId, int level) {
        if (beneficiaryUserId == null) {
            return;
        }
        BigDecimal rate = resolveBeneficiaryCommissionRate(beneficiaryUserId, level);
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        LambdaQueryWrapper<CommissionRecordEntity> existsQuery = new LambdaQueryWrapper<>();
        existsQuery.eq(CommissionRecordEntity::getOrderId, order.getId())
                .eq(CommissionRecordEntity::getCommissionLevel, level)
                .eq(CommissionRecordEntity::getBeneficiaryUserId, beneficiaryUserId)
                .last("LIMIT 1");
        if (iCommissionRecordService.count(existsQuery) > 0) {
            return;
        }

        BigDecimal commissionAmount = InviteSupport.calcCommission(order.getPayAmount(), rate);
        if (commissionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        CommissionRecordEntity record = new CommissionRecordEntity();
        record.setBeneficiaryUserId(beneficiaryUserId);
        record.setSourceUserId(order.getUserId());
        record.setOrderId(order.getId());
        record.setOrderNo(order.getOrderNo());
        record.setCommissionLevel(level);
        record.setOrderAmount(order.getPayAmount());
        record.setCommissionRate(rate);
        record.setCommissionAmount(commissionAmount);
        record.setStatus(InviteSupport.COMMISSION_STATUS_SETTLED);
        iCommissionRecordService.save(record);

        String title = level == InviteSupport.LEVEL1 ? "一级邀请佣金" : "二级邀请佣金";
        walletService.creditCommission(beneficiaryUserId, commissionAmount, order.getOrderNo(), title);
    }

    private InviteTeamSummaryVo buildTeamSummary(Long userId) {
        long level1Count = countTeamMembers(userId, InviteSupport.LEVEL1);
        long level2Count = countTeamMembers(userId, InviteSupport.LEVEL2);

        InviteCommissionSummaryVo commissionSummary = getCommissionSummary(userId);

        InviteTeamSummaryVo vo = new InviteTeamSummaryVo();
        vo.setLevel1Count(Math.toIntExact(level1Count));
        vo.setLevel2Count(Math.toIntExact(level2Count));
        vo.setInviteCount(Math.toIntExact(level1Count + level2Count));
        vo.setTotalEarnings(commissionSummary.getTotal());
        return vo;
    }

    private long countTeamMembers(Long userId, int level) {
        LambdaQueryWrapper<InviteRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (level == InviteSupport.LEVEL2) {
            queryWrapper.eq(InviteRelationEntity::getInviterLevel2UserId, userId);
        } else {
            queryWrapper.eq(InviteRelationEntity::getInviterUserId, userId);
        }
        return iInviteRelationService.count(queryWrapper);
    }

    private BigDecimal sumCommission(Long userId, int level) {
        LambdaQueryWrapper<CommissionRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommissionRecordEntity::getBeneficiaryUserId, userId)
                .eq(CommissionRecordEntity::getCommissionLevel, level);
        List<CommissionRecordEntity> records = iCommissionRecordService.list(queryWrapper);
        BigDecimal total = BigDecimal.ZERO;
        for (CommissionRecordEntity record : records) {
            if (record.getCommissionAmount() != null) {
                total = total.add(record.getCommissionAmount());
            }
        }
        return total;
    }

    private long countCommission(Long userId, int level) {
        LambdaQueryWrapper<CommissionRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommissionRecordEntity::getBeneficiaryUserId, userId)
                .eq(CommissionRecordEntity::getCommissionLevel, level);
        return iCommissionRecordService.count(queryWrapper);
    }

    private UserEntity requireUser(Long userId) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            user = new UserEntity();
            user.setUserId(userId);
            user.setUserName("用户" + userId);
            user.setInvitationCode("");
        }
        return user;
    }

    private Map<Long, UserEntity> loadUserMap(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<UserEntity> users = iUserService.listByIds(userIds);
        Map<Long, UserEntity> map = new HashMap<>();
        for (UserEntity user : users) {
            map.put(user.getUserId(), user);
        }
        return map;
    }

    private int normalizeLevel(Integer level) {
        if (level != null && level == InviteSupport.LEVEL2) {
            return InviteSupport.LEVEL2;
        }
        return InviteSupport.LEVEL1;
    }

    private <T> Page<T> initBoundedPage(Long page, Long limit) {
        Page<T> pageObj = PageUtil.initPage(page, limit);
        if (pageObj.getSize() > MAX_PAGE_LIMIT) {
            pageObj.setSize(MAX_PAGE_LIMIT);
        }
        return pageObj;
    }

    private BigDecimal resolveBeneficiaryCommissionRate(Long beneficiaryUserId, int level) {
        UserEntity user = iUserService.getById(beneficiaryUserId);
        if (InviteSupport.LEVEL1 == level) {
            return InviteSupport.resolveCommissionRate(
                    user == null ? null : user.getCommissionRateLevel1(),
                    getOptionalConfigValue(SysConfigConstant.Invite.INVITE_COMMISSION_RATE_LEVEL1),
                    InviteSupport.defaultRateLevel1());
        }
        return InviteSupport.resolveCommissionRate(
                user == null ? null : user.getCommissionRateLevel2(),
                getOptionalConfigValue(SysConfigConstant.Invite.INVITE_COMMISSION_RATE_LEVEL2),
                InviteSupport.defaultRateLevel2());
    }

    private String getOptionalConfigValue(String key) {
        try {
            String value = SysConfigUtil.getConfigValue(key);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        } catch (Exception ex) {
            // 配置缺失时使用 InviteSupport 内置默认值
        }
        return null;
    }
}

package com.sqx.app.lvyou.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.coupon.db.service.ICouponTemplateService;
import com.sqx.app.lvyou.coupon.db.service.IUserCouponService;
import com.sqx.app.lvyou.coupon.domain.bo.CouponClaimBo;
import com.sqx.app.lvyou.coupon.domain.bo.CouponTemplateInfoBo;
import com.sqx.app.lvyou.coupon.domain.bo.CouponTemplatePageBo;
import com.sqx.app.lvyou.coupon.domain.bo.CouponTemplateSaveBo;
import com.sqx.app.lvyou.coupon.domain.bo.CouponTemplateUpdateBo;
import com.sqx.app.lvyou.coupon.domain.entity.CouponTemplateEntity;
import com.sqx.app.lvyou.coupon.domain.entity.UserCouponEntity;
import com.sqx.app.lvyou.coupon.domain.vo.CouponAvailableVo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponCenterVo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponCountVo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponMineVo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponTemplateVo;
import com.sqx.app.lvyou.coupon.enums.CouponScope;
import com.sqx.app.lvyou.coupon.enums.CouponTemplateStatus;
import com.sqx.app.lvyou.coupon.enums.UserCouponStatus;
import com.sqx.app.lvyou.coupon.service.CouponService;
import com.sqx.app.lvyou.coupon.support.CouponSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 优惠券业务服务实现
 */
@Service
public class CouponServiceImpl implements CouponService {

    private static final int TEMPLATE_ONLINE = CouponTemplateStatus.ONLINE.getCode();

    @Resource
    private ICouponTemplateService iCouponTemplateService;

    @Resource
    private IUserCouponService iUserCouponService;

    @Override
    public IPage<CouponTemplateVo> templatePage(CouponTemplatePageBo pageBo) {
        Page<CouponTemplateEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<CouponTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(pageBo.getId()), CouponTemplateEntity::getId, pageBo.getId())
                .like(StringUtils.isNotBlank(pageBo.getTemplateCode()), CouponTemplateEntity::getTemplateCode, pageBo.getTemplateCode())
                .like(StringUtils.isNotBlank(pageBo.getName()), CouponTemplateEntity::getName, pageBo.getName())
                .eq(StringUtils.isNotBlank(pageBo.getScope()), CouponTemplateEntity::getScope, pageBo.getScope())
                .eq(Objects.nonNull(pageBo.getStatus()), CouponTemplateEntity::getStatus, pageBo.getStatus())
                .orderByDesc(CouponTemplateEntity::getId);

        IPage<CouponTemplateEntity> entityPage = iCouponTemplateService.page(page, queryWrapper);
        List<CouponTemplateVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (CouponTemplateEntity record : entityPage.getRecords()) {
                voList.add(toTemplateVo(record));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void templateSave(CouponTemplateSaveBo saveBo) {
        validateScope(saveBo.getScope());
        ensureTemplateCodeUnique(saveBo.getTemplateCode(), null);

        CouponTemplateEntity entity = new CouponTemplateEntity();
        BeanUtils.copyProperties(saveBo, entity);
        entity.setClaimedCount(0);
        iCouponTemplateService.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void templateUpdate(CouponTemplateUpdateBo updateBo) {
        CouponTemplateEntity existing = iCouponTemplateService.getById(updateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.COUPON_TEMPLATE_NOT_FOUND);
        }
        validateScope(updateBo.getScope());
        ensureTemplateCodeUnique(updateBo.getTemplateCode(), updateBo.getId());

        CouponTemplateEntity entity = new CouponTemplateEntity();
        BeanUtils.copyProperties(updateBo, entity);
        entity.setClaimedCount(existing.getClaimedCount());
        iCouponTemplateService.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void templateDelete(CouponTemplateInfoBo infoBo) {
        CouponTemplateEntity existing = iCouponTemplateService.getById(infoBo.getId());
        if (existing == null) {
            throw new BizException(AppError.COUPON_TEMPLATE_NOT_FOUND);
        }
        int claimedCount = existing.getClaimedCount() == null ? 0 : existing.getClaimedCount();
        if (claimedCount > 0) {
            throw new BizException(AppError.COUPON_TEMPLATE_HAS_CLAIMED);
        }
        iCouponTemplateService.removeById(infoBo.getId());
    }

    @Override
    public List<CouponCenterVo> center(Long userId) {
        LambdaQueryWrapper<CouponTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponTemplateEntity::getStatus, TEMPLATE_ONLINE)
                .orderByDesc(CouponTemplateEntity::getId);
        List<CouponTemplateEntity> templates = iCouponTemplateService.list(queryWrapper);
        if (CollectionUtils.isEmpty(templates)) {
            return new ArrayList<>();
        }

        Set<String> claimedTemplateCodes = loadActiveClaimedTemplateCodes(userId);
        List<CouponCenterVo> result = new ArrayList<>();
        for (CouponTemplateEntity template : templates) {
            if (!hasRemainingStock(template)) {
                continue;
            }
            CouponCenterVo vo = new CouponCenterVo();
            vo.setTemplateId(template.getTemplateCode());
            vo.setName(template.getName());
            vo.setAmount(template.getAmount());
            vo.setMinAmount(template.getMinAmount());
            vo.setScope(template.getScope());
            vo.setValidDays(template.getValidDays());
            vo.setDesc(template.getDescription());
            vo.setClaimed(claimedTemplateCodes.contains(template.getTemplateCode()));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponMineVo claim(Long userId, CouponClaimBo claimBo) {
        String templateCode = claimBo.getTemplateId();
        CouponTemplateEntity template = loadOnlineTemplate(templateCode);
        if (!hasRemainingStock(template)) {
            throw new BizException(AppError.COUPON_CLAIM_LIMIT);
        }

        int perUserLimit = template.getPerUserLimit() == null ? 1 : template.getPerUserLimit();
        long userClaimedCount = countUserActiveClaims(userId, template.getId());
        if (userClaimedCount >= perUserLimit) {
            throw new BizException(AppError.COUPON_ALREADY_CLAIMED);
        }

        boolean stockUpdated = incrementClaimedCount(template.getId(), template.getTotalCount());
        if (!stockUpdated) {
            throw new BizException(AppError.COUPON_CLAIM_LIMIT);
        }

        LocalDateTime now = LocalDateTime.now();
        UserCouponEntity userCoupon = new UserCouponEntity();
        userCoupon.setCouponCode(CouponSupport.generateCouponCode(userId));
        userCoupon.setUserId(userId);
        userCoupon.setTemplateId(template.getId());
        userCoupon.setTemplateCode(template.getTemplateCode());
        userCoupon.setName(template.getName());
        userCoupon.setAmount(template.getAmount());
        userCoupon.setMinAmount(template.getMinAmount());
        userCoupon.setScope(template.getScope());
        userCoupon.setStatus(UserCouponStatus.UNUSED.getCode());
        userCoupon.setValidStart(now);
        int validDays = template.getValidDays() == null ? 30 : template.getValidDays();
        userCoupon.setValidEnd(now.plusDays(validDays).withHour(23).withMinute(59).withSecond(59));
        iUserCouponService.save(userCoupon);

        return toMineVo(userCoupon, template.getDescription(), now);
    }

    @Override
    public List<CouponMineVo> mine(Long userId, String status) {
        LocalDateTime now = LocalDateTime.now();
        refreshExpiredCoupons(userId, now);

        LambdaQueryWrapper<UserCouponEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCouponEntity::getUserId, userId)
                .orderByDesc(UserCouponEntity::getId);
        List<UserCouponEntity> coupons = iUserCouponService.list(queryWrapper);
        if (CollectionUtils.isEmpty(coupons)) {
            return new ArrayList<>();
        }

        Map<Long, String> descriptionMap = loadTemplateDescriptionMap(coupons);
        UserCouponStatus filterStatus = UserCouponStatus.fromAppCode(status);
        List<CouponMineVo> result = new ArrayList<>();
        for (UserCouponEntity coupon : coupons) {
            CouponMineVo vo = toMineVo(coupon, descriptionMap.get(coupon.getTemplateId()), now);
            if (filterStatus != null && !filterStatus.getAppCode().equals(vo.getStatus())) {
                continue;
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<CouponAvailableVo> available(Long userId, String scope, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();
        refreshExpiredCoupons(userId, now);

        String orderScope = StringUtils.defaultIfBlank(scope, CouponScope.ALL.getCode());
        BigDecimal orderAmount = amount == null ? BigDecimal.ZERO : amount;

        LambdaQueryWrapper<UserCouponEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCouponEntity::getUserId, userId)
                .eq(UserCouponEntity::getStatus, UserCouponStatus.UNUSED.getCode())
                .orderByDesc(UserCouponEntity::getAmount)
                .orderByDesc(UserCouponEntity::getId);
        List<UserCouponEntity> coupons = iUserCouponService.list(queryWrapper);
        if (CollectionUtils.isEmpty(coupons)) {
            return new ArrayList<>();
        }

        Map<Long, String> descriptionMap = loadTemplateDescriptionMap(coupons);
        List<CouponAvailableVo> result = new ArrayList<>();
        for (UserCouponEntity coupon : coupons) {
            if (coupon.getValidStart() != null && now.isBefore(coupon.getValidStart())) {
                continue;
            }
            if (coupon.getValidEnd() != null && now.isAfter(coupon.getValidEnd())) {
                continue;
            }
            CouponAvailableVo vo = new CouponAvailableVo();
            CouponMineVo base = toMineVo(coupon, descriptionMap.get(coupon.getTemplateId()), now);
            BeanUtils.copyProperties(base, vo);
            boolean applicable = CouponSupport.isApplicable(coupon, orderScope, orderAmount, now);
            vo.setApplicable(applicable);
            vo.setDiscount(CouponSupport.calcDiscount(coupon, orderScope, orderAmount, now));
            result.add(vo);
        }

        result.sort(Comparator.comparing(CouponAvailableVo::getApplicable).reversed()
                .thenComparing(CouponAvailableVo::getDiscount, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    @Override
    public CouponCountVo countAvailable(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        refreshExpiredCoupons(userId, now);

        LambdaQueryWrapper<UserCouponEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCouponEntity::getUserId, userId)
                .eq(UserCouponEntity::getStatus, UserCouponStatus.UNUSED.getCode())
                .and(wrapper -> wrapper.isNull(UserCouponEntity::getValidEnd)
                        .or()
                        .ge(UserCouponEntity::getValidEnd, now));
        long count = iUserCouponService.count(queryWrapper);

        CouponCountVo vo = new CouponCountVo();
        vo.setCount((int) count);
        return vo;
    }

    private CouponTemplateVo toTemplateVo(CouponTemplateEntity entity) {
        CouponTemplateVo vo = new CouponTemplateVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setScopeLabel(CouponScope.labelOf(entity.getScope()));
        vo.setStatusLabel(CouponTemplateStatus.labelOf(entity.getStatus()));
        return vo;
    }

    private CouponMineVo toMineVo(UserCouponEntity coupon, String description, LocalDateTime now) {
        CouponMineVo vo = new CouponMineVo();
        vo.setId(coupon.getId());
        vo.setTemplateId(coupon.getTemplateCode());
        vo.setName(coupon.getName());
        vo.setAmount(coupon.getAmount());
        vo.setMinAmount(coupon.getMinAmount());
        vo.setScope(coupon.getScope());
        vo.setValidFrom(CouponSupport.formatDate(coupon.getValidStart()));
        vo.setValidTo(CouponSupport.formatDate(coupon.getValidEnd()));
        vo.setDesc(description);
        vo.setStatus(CouponSupport.resolveDisplayStatus(coupon, now).getAppCode());
        return vo;
    }

    private void validateScope(String scope) {
        if (!CouponScope.isValid(scope)) {
            throw new BizException(AppError.COUPON_SCOPE_INVALID);
        }
    }

    private void ensureTemplateCodeUnique(String templateCode, Long excludeId) {
        LambdaQueryWrapper<CouponTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponTemplateEntity::getTemplateCode, templateCode);
        if (excludeId != null) {
            queryWrapper.ne(CouponTemplateEntity::getId, excludeId);
        }
        if (iCouponTemplateService.count(queryWrapper) > 0) {
            throw new BizException(AppError.COUPON_TEMPLATE_CODE_DUPLICATE);
        }
    }

    private CouponTemplateEntity loadOnlineTemplate(String templateCode) {
        LambdaQueryWrapper<CouponTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponTemplateEntity::getTemplateCode, templateCode)
                .eq(CouponTemplateEntity::getStatus, TEMPLATE_ONLINE);
        CouponTemplateEntity template = iCouponTemplateService.getOne(queryWrapper);
        if (template == null) {
            throw new BizException(AppError.COUPON_TEMPLATE_NOT_FOUND);
        }
        return template;
    }

    private boolean hasRemainingStock(CouponTemplateEntity template) {
        int totalCount = template.getTotalCount() == null ? 0 : template.getTotalCount();
        if (totalCount <= 0) {
            return true;
        }
        int claimedCount = template.getClaimedCount() == null ? 0 : template.getClaimedCount();
        return claimedCount < totalCount;
    }

    private Set<String> loadActiveClaimedTemplateCodes(Long userId) {
        LambdaQueryWrapper<UserCouponEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCouponEntity::getUserId, userId)
                .in(UserCouponEntity::getStatus, UserCouponStatus.UNUSED.getCode(), UserCouponStatus.USED.getCode());
        List<UserCouponEntity> coupons = iUserCouponService.list(queryWrapper);
        if (CollectionUtils.isEmpty(coupons)) {
            return new HashSet<>();
        }
        return coupons.stream()
                .map(UserCouponEntity::getTemplateCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }

    private long countUserActiveClaims(Long userId, Long templateId) {
        LambdaQueryWrapper<UserCouponEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCouponEntity::getUserId, userId)
                .eq(UserCouponEntity::getTemplateId, templateId)
                .in(UserCouponEntity::getStatus, UserCouponStatus.UNUSED.getCode(), UserCouponStatus.USED.getCode());
        return iUserCouponService.count(queryWrapper);
    }

    private boolean incrementClaimedCount(Long templateId, Integer totalCount) {
        LambdaUpdateWrapper<CouponTemplateEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CouponTemplateEntity::getId, templateId)
                .setSql("claimed_count = claimed_count + 1");
        if (totalCount != null && totalCount > 0) {
            updateWrapper.lt(CouponTemplateEntity::getClaimedCount, totalCount);
        }
        return iCouponTemplateService.update(updateWrapper);
    }

    private void refreshExpiredCoupons(Long userId, LocalDateTime now) {
        LambdaUpdateWrapper<UserCouponEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserCouponEntity::getUserId, userId)
                .eq(UserCouponEntity::getStatus, UserCouponStatus.UNUSED.getCode())
                .isNotNull(UserCouponEntity::getValidEnd)
                .lt(UserCouponEntity::getValidEnd, now)
                .set(UserCouponEntity::getStatus, UserCouponStatus.EXPIRED.getCode());
        iUserCouponService.update(updateWrapper);
    }

    private Map<Long, String> loadTemplateDescriptionMap(List<UserCouponEntity> coupons) {
        Set<Long> templateIds = coupons.stream()
                .map(UserCouponEntity::getTemplateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(templateIds)) {
            return new HashMap<>();
        }
        List<CouponTemplateEntity> templates = iCouponTemplateService.listByIds(templateIds);
        Map<Long, String> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(templates)) {
            for (CouponTemplateEntity template : templates) {
                map.put(template.getId(), template.getDescription());
            }
        }
        return map;
    }
}

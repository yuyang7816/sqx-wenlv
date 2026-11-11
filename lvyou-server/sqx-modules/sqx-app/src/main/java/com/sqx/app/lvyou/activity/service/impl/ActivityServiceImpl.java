package com.sqx.app.lvyou.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.activity.db.service.IActivityEnrollmentService;
import com.sqx.app.lvyou.activity.db.service.IActivityService;
import com.sqx.app.lvyou.activity.domain.bo.*;
import com.sqx.app.lvyou.activity.domain.entity.ActivityEnrollmentEntity;
import com.sqx.app.lvyou.activity.domain.entity.ActivityEntity;
import com.sqx.app.lvyou.activity.domain.vo.*;
import com.sqx.app.lvyou.activity.enums.ActivityPhase;
import com.sqx.app.lvyou.activity.enums.ActivityStatus;
import com.sqx.app.lvyou.activity.service.ActivityService;
import com.sqx.app.lvyou.favorite.enums.AssetTargetType;
import com.sqx.app.lvyou.favorite.service.FootprintService;
import com.sqx.app.lvyou.activity.util.ActivityJsonUtil;
import com.sqx.app.lvyou.activity.util.ActivityPhaseUtil;
import com.sqx.app.lvyou.activity.util.ActivityTimeUtil;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.destination.support.DestinationDisplaySupport;
import com.sqx.app.lvyou.destination.support.PublishDestinationResolver;
import com.sqx.app.lvyou.home.domain.vo.HomeRecommendItemVo;
import com.sqx.app.lvyou.home.util.RecommendTagUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 精彩活动业务服务实现
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    private static final int STATUS_ONLINE = ActivityStatus.ONLINE.getCode();

    @Resource
    private IActivityService iActivityService;

    @Resource
    private IActivityEnrollmentService iActivityEnrollmentService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private IUserService iUserService;

    @Resource
    private FootprintService footprintService;

    @Resource
    private PublishDestinationResolver publishDestinationResolver;

    @Override
    public List<HomeRecommendItemVo> listRecommend(Long destinationId, int limit) {
        if (destinationId == null || limit <= 0) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ActivityEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityEntity::getStatus, STATUS_ONLINE)
                .eq(ActivityEntity::getDestinationId, destinationId)
                .orderByAsc(ActivityEntity::getSort)
                .orderByDesc(ActivityEntity::getId)
                .last("LIMIT " + limit);
        List<ActivityEntity> entities = iActivityService.list(queryWrapper);
        List<HomeRecommendItemVo> items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entities)) {
            for (ActivityEntity entity : entities) {
                items.add(toRecommendItem(entity));
            }
        }
        return items;
    }

    @Override
    public IPage<ActivityAppVo> appPage(ActivityAppListBo listBo) {
        if (!ActivityPhase.isValidFilter(listBo.getStatus())) {
            throw new BizException(AppError.ACTIVITY_PHASE_INVALID);
        }
        Page<ActivityEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<ActivityEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityEntity::getStatus, STATUS_ONLINE)
                .eq(Objects.nonNull(listBo.getDestinationId()), ActivityEntity::getDestinationId, listBo.getDestinationId())
                .like(StringUtils.isNotBlank(listBo.getName()), ActivityEntity::getName, listBo.getName());
        applyPhaseFilter(queryWrapper, listBo.getStatus());
        queryWrapper.orderByAsc(ActivityEntity::getSort)
                .orderByDesc(ActivityEntity::getId);

        IPage<ActivityEntity> entityPage = iActivityService.page(page, queryWrapper);
        List<ActivityAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            LocalDateTime now = LocalDateTime.now();
            for (ActivityEntity record : entityPage.getRecords()) {
                voList.add(toAppVo(record, now));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public ActivityAppDetailVo appDetail(Long id, Long userId) {
        ActivityEntity entity = iActivityService.getById(id);
        if (entity == null || !Objects.equals(entity.getStatus(), STATUS_ONLINE)) {
            throw new BizException(AppError.ACTIVITY_NOT_FOUND);
        }
        ActivityAppDetailVo vo = toAppDetailVo(entity, LocalDateTime.now());
        vo.setEnrolled(userId != null && hasEnrolled(id, userId));
        footprintService.recordAsync(userId, AssetTargetType.ACTIVITY.getCode(), id);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityEnrollVo enroll(Long activityId, Long userId) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        ActivityEntity activity = iActivityService.getById(activityId);
        if (activity == null || !Objects.equals(activity.getStatus(), STATUS_ONLINE)) {
            throw new BizException(AppError.ACTIVITY_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        String phase = ActivityPhaseUtil.resolvePhase(activity.getStartTime(), activity.getEndTime(), now);
        if (ActivityPhase.ENDED.getCode().equals(phase)) {
            throw new BizException(AppError.ACTIVITY_ENROLL_ENDED);
        }

        if (hasEnrolled(activityId, userId)) {
            throw new BizException(AppError.ACTIVITY_ENROLLMENT_DUPLICATE);
        }

        int enrollLimit = activity.getEnrollLimit() == null ? 0 : activity.getEnrollLimit();
        int enrollCount = activity.getEnrollCount() == null ? 0 : activity.getEnrollCount();
        if (enrollLimit > 0 && enrollCount >= enrollLimit) {
            throw new BizException(AppError.ACTIVITY_ENROLL_FULL);
        }

        ActivityEnrollmentEntity enrollment = new ActivityEnrollmentEntity();
        enrollment.setActivityId(activityId);
        enrollment.setUserId(userId);
        try {
            iActivityEnrollmentService.save(enrollment);
        } catch (DuplicateKeyException ex) {
            throw new BizException(AppError.ACTIVITY_ENROLLMENT_DUPLICATE);
        }

        LambdaUpdateWrapper<ActivityEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivityEntity::getId, activityId)
                .and(wrapper -> wrapper.eq(ActivityEntity::getEnrollLimit, 0)
                        .or()
                        .apply("enroll_count < enroll_limit"))
                .setSql("enroll_count = enroll_count + 1");
        boolean updated = iActivityService.update(new ActivityEntity(), updateWrapper);
        if (!updated) {
            throw new BizException(AppError.ACTIVITY_ENROLL_FULL);
        }

        ActivityEntity refreshed = iActivityService.getById(activityId);
        ActivityEnrollVo vo = new ActivityEnrollVo();
        vo.setActivityId(activityId);
        vo.setEnrollmentId(enrollment.getId());
        vo.setParticipants(refreshed == null ? enrollCount + 1 : refreshed.getEnrollCount());
        return vo;
    }

    @Override
    public IPage<ActivityVo> pageInfo(ActivityPageBo pageBo) {
        Page<ActivityEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<ActivityEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageBo.getName()), ActivityEntity::getName, pageBo.getName())
                .eq(Objects.nonNull(pageBo.getDestinationId()), ActivityEntity::getDestinationId, pageBo.getDestinationId())
                .eq(Objects.nonNull(pageBo.getStatus()), ActivityEntity::getStatus, pageBo.getStatus());
        applyPhaseFilter(queryWrapper, pageBo.getPhase());
        queryWrapper.orderByAsc(ActivityEntity::getSort)
                .orderByDesc(ActivityEntity::getId);

        IPage<ActivityEntity> entityPage = iActivityService.page(page, queryWrapper);
        Map<Long, String> destinationNameMap = loadDestinationNameMap(entityPage.getRecords());
        List<ActivityVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            LocalDateTime now = LocalDateTime.now();
            for (ActivityEntity record : entityPage.getRecords()) {
                voList.add(toAdminVo(record, destinationNameMap, now));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(ActivitySaveBo saveBo) {
        validateTimeRange(saveBo.getStartTime(), saveBo.getEndTime());

        ActivityEntity entity = new ActivityEntity();
        fillEntityFromSaveBo(entity, saveBo);
        entity.setDestinationId(publishDestinationResolver.requireFromCoordinates(saveBo.getLatitude(), saveBo.getLongitude()));
        fillDefaults(entity);
        iActivityService.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(ActivityUpdateBo updateBo) {
        ActivityEntity existing = iActivityService.getById(updateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.ACTIVITY_NOT_FOUND);
        }
        validateTimeRange(updateBo.getStartTime(), updateBo.getEndTime());

        ActivityEntity entity = new ActivityEntity();
        fillEntityFromSaveBo(entity, updateBo);
        entity.setDestinationId(publishDestinationResolver.requireFromCoordinates(updateBo.getLatitude(), updateBo.getLongitude()));
        entity.setId(updateBo.getId());
        entity.setEnrollCount(existing.getEnrollCount());
        iActivityService.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ActivityInfoBo infoBo) {
        ActivityEntity existing = iActivityService.getById(infoBo.getId());
        if (existing == null) {
            throw new BizException(AppError.ACTIVITY_NOT_FOUND);
        }
        LambdaQueryWrapper<ActivityEnrollmentEntity> enrollmentQuery = new LambdaQueryWrapper<>();
        enrollmentQuery.eq(ActivityEnrollmentEntity::getActivityId, infoBo.getId());
        iActivityEnrollmentService.remove(enrollmentQuery);
        iActivityService.removeById(infoBo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateState(ActivityUpdateStateBo updateStateBo) {
        ActivityEntity existing = iActivityService.getById(updateStateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.ACTIVITY_NOT_FOUND);
        }
        LambdaUpdateWrapper<ActivityEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivityEntity::getId, updateStateBo.getId())
                .set(ActivityEntity::getStatus, updateStateBo.getStatus());
        iActivityService.update(new ActivityEntity(), updateWrapper);
    }

    private void applyPhaseFilter(LambdaQueryWrapper<ActivityEntity> queryWrapper, String phaseCode) {
        if (StringUtils.isBlank(phaseCode) || "all".equalsIgnoreCase(phaseCode)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (ActivityPhase.UPCOMING.getCode().equalsIgnoreCase(phaseCode)) {
            queryWrapper.isNotNull(ActivityEntity::getStartTime)
                    .gt(ActivityEntity::getStartTime, now);
            return;
        }
        if (ActivityPhase.ENDED.getCode().equalsIgnoreCase(phaseCode)) {
            queryWrapper.isNotNull(ActivityEntity::getEndTime)
                    .lt(ActivityEntity::getEndTime, now);
            return;
        }
        if (ActivityPhase.ONGOING.getCode().equalsIgnoreCase(phaseCode)) {
            queryWrapper.and(wrapper -> wrapper.isNull(ActivityEntity::getStartTime)
                    .or()
                    .le(ActivityEntity::getStartTime, now))
                    .and(wrapper -> wrapper.isNull(ActivityEntity::getEndTime)
                            .or()
                            .ge(ActivityEntity::getEndTime, now));
        }
    }

    private boolean hasEnrolled(Long activityId, Long userId) {
        LambdaQueryWrapper<ActivityEnrollmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityEnrollmentEntity::getActivityId, activityId)
                .eq(ActivityEnrollmentEntity::getUserId, userId);
        return iActivityEnrollmentService.count(queryWrapper) > 0;
    }

    private void validateDestination(Long destinationId) {
        if (destinationId == null) {
            return;
        }
        DestinationEntity destination = iDestinationService.getById(destinationId);
        if (destination == null) {
            throw new BizException(AppError.DESTINATION_NOT_FOUND);
        }
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new BizException(AppError.ACTIVITY_TIME_INVALID);
        }
    }

    private void fillEntityFromSaveBo(ActivityEntity entity, ActivitySaveBo saveBo) {
        BeanUtils.copyProperties(saveBo, entity, "tags", "highlights", "notices");
        entity.setTag(ActivityJsonUtil.toJsonArray(saveBo.getTags()));
        entity.setHighlights(ActivityJsonUtil.toHighlightsJson(saveBo.getHighlights()));
        entity.setNotices(ActivityJsonUtil.toJsonArray(saveBo.getNotices()));
        if (entity.getEnrollLimit() == null) {
            entity.setEnrollLimit(0);
        }
    }

    private void fillDefaults(ActivityEntity entity) {
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getEnrollCount() == null) {
            entity.setEnrollCount(0);
        }
        if (entity.getEnrollLimit() == null) {
            entity.setEnrollLimit(0);
        }
    }

    private Map<Long, String> loadDestinationNameMap(List<ActivityEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new HashMap<>();
        }
        List<Long> destinationIds = records.stream()
                .map(ActivityEntity::getDestinationId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(destinationIds)) {
            return new HashMap<>();
        }
        List<DestinationEntity> destinations = iDestinationService.listByIds(destinationIds);
        Map<Long, String> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(destinations)) {
            for (DestinationEntity destination : destinations) {
                map.put(destination.getId(), destination.getName());
            }
        }
        return map;
    }

    private HomeRecommendItemVo toRecommendItem(ActivityEntity entity) {
        HomeRecommendItemVo vo = new HomeRecommendItemVo();
        vo.setId(entity.getId());
        vo.setTitle(entity.getName());
        vo.setCover(StringUtils.defaultString(entity.getCoverImage()));
        vo.setPrice(entity.getPriceFrom());
        vo.setTag(RecommendTagUtil.resolveTag(entity.getTag()));
        vo.setAddress(StringUtils.defaultString(entity.getAddress()));
        return vo;
    }

    private ActivityAppVo toAppVo(ActivityEntity entity, LocalDateTime now) {
        ActivityAppVo vo = new ActivityAppVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setAddress(StringUtils.defaultString(entity.getAddress()));
        vo.setTime(ActivityTimeUtil.formatTimeRange(entity.getStartTime(), entity.getEndTime()));
        String phase = ActivityPhaseUtil.resolvePhase(entity.getStartTime(), entity.getEndTime(), now);
        vo.setStatus(phase);
        vo.setStatusLabel(ActivityPhaseUtil.resolvePhaseLabel(phase));
        vo.setParticipants(entity.getEnrollCount() == null ? 0 : entity.getEnrollCount());
        vo.setCover(StringUtils.defaultString(entity.getCoverImage()));
        vo.setTags(ActivityJsonUtil.parseTags(entity.getTag()));
        vo.setPrice(entity.getPriceFrom());
        vo.setPriceTip("线上仅报名，费用请在线下签到时支付");
        return vo;
    }

    private ActivityAppDetailVo toAppDetailVo(ActivityEntity entity, LocalDateTime now) {
        ActivityAppDetailVo vo = new ActivityAppDetailVo();
        BeanUtils.copyProperties(toAppVo(entity, now), vo);
        vo.setDesc(StringUtils.defaultString(entity.getDescription()));
        vo.setHighlights(ActivityJsonUtil.parseHighlights(entity.getHighlights()));
        vo.setNotices(ActivityJsonUtil.parseNotices(entity.getNotices()));
        vo.setEnrollLimit(entity.getEnrollLimit() == null ? 0 : entity.getEnrollLimit());
        vo.setLatitude(entity.getLatitude());
        vo.setLongitude(entity.getLongitude());
        return vo;
    }

    private ActivityVo toAdminVo(ActivityEntity entity, Map<Long, String> destinationNameMap, LocalDateTime now) {
        ActivityVo vo = new ActivityVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setTags(ActivityJsonUtil.parseTags(entity.getTag()));
        vo.setHighlights(ActivityJsonUtil.parseHighlights(entity.getHighlights()));
        vo.setNotices(ActivityJsonUtil.parseNotices(entity.getNotices()));
        String phase = ActivityPhaseUtil.resolvePhase(entity.getStartTime(), entity.getEndTime(), now);
        vo.setPhase(phase);
        vo.setPhaseLabel(ActivityPhaseUtil.resolvePhaseLabel(phase));
        vo.setStatusLabel(ActivityStatus.labelOf(entity.getStatus()));
        vo.setDestinationName(DestinationDisplaySupport.resolveLabel(
                destinationNameMap, entity.getDestinationId(), entity.getAddress()));
        return vo;
    }
}

package com.sqx.app.lvyou.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.destination.support.DestinationDisplaySupport;
import com.sqx.app.lvyou.destination.support.PublishDestinationResolver;
import com.sqx.app.lvyou.notice.db.service.INoticeService;
import com.sqx.app.lvyou.notice.domain.bo.*;
import com.sqx.app.lvyou.notice.domain.entity.NoticeEntity;
import com.sqx.app.lvyou.notice.domain.vo.NoticeAppVo;
import com.sqx.app.lvyou.notice.domain.vo.NoticeMarqueeVo;
import com.sqx.app.lvyou.notice.domain.vo.NoticeVo;
import com.sqx.app.lvyou.notice.enums.NoticeCategory;
import com.sqx.app.lvyou.notice.enums.NoticeState;
import com.sqx.app.lvyou.notice.service.NoticeService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 公告业务服务实现
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    private static final int PUBLISHED = NoticeState.PUBLISHED.getCode();
    private static final int PINNED = 1;
    private static final int NEW_DAYS = 7;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private INoticeService iNoticeService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private PublishDestinationResolver publishDestinationResolver;

    @Override
    public IPage<NoticeAppVo> appPage(NoticeAppListBo listBo) {
        Page<NoticeEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<NoticeEntity> queryWrapper = buildPublishedQuery();
        applyDestinationFilter(queryWrapper, listBo.getDestinationId());
        if (StringUtils.isNotBlank(listBo.getCategory()) && !"all".equalsIgnoreCase(listBo.getCategory())) {
            queryWrapper.eq(NoticeEntity::getCategory, listBo.getCategory());
        }
        applyAppOrder(queryWrapper);

        IPage<NoticeEntity> entityPage = iNoticeService.page(page, queryWrapper);
        List<NoticeAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (NoticeEntity record : entityPage.getRecords()) {
                voList.add(toAppVo(record, false));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public NoticeMarqueeVo marquee(Long destinationId) {
        LambdaQueryWrapper<NoticeEntity> queryWrapper = buildPublishedQuery();
        queryWrapper.eq(NoticeEntity::getPinned, PINNED);
        applyDestinationFilter(queryWrapper, destinationId);
        applyAppOrder(queryWrapper);
        queryWrapper.last("LIMIT 1");

        List<NoticeEntity> entities = iNoticeService.list(queryWrapper);
        List<NoticeAppVo> items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entities)) {
            items.add(toAppVo(entities.get(0), false));
        }

        NoticeMarqueeVo vo = new NoticeMarqueeVo();
        vo.setItems(items);
        vo.setText(buildMarqueeText(items));
        return vo;
    }

    @Override
    public NoticeAppVo appDetail(Long id) {
        NoticeEntity entity = iNoticeService.getById(id);
        if (entity == null || !isPublishedVisible(entity)) {
            throw new BizException(AppError.NOTICE_NOT_FOUND);
        }
        return toAppVo(entity, true);
    }

    @Override
    public IPage<NoticeVo> pageInfo(NoticePageBo pageBo) {
        Page<NoticeEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<NoticeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageBo.getTitle()), NoticeEntity::getTitle, pageBo.getTitle())
                .eq(StringUtils.isNotBlank(pageBo.getCategory()), NoticeEntity::getCategory, pageBo.getCategory())
                .eq(Objects.nonNull(pageBo.getDestinationId()), NoticeEntity::getDestinationId, pageBo.getDestinationId())
                .eq(Objects.nonNull(pageBo.getState()), NoticeEntity::getState, pageBo.getState())
                .orderByDesc(NoticeEntity::getPinned)
                .orderByDesc(NoticeEntity::getPublishTime)
                .orderByAsc(NoticeEntity::getSort)
                .orderByDesc(NoticeEntity::getId);

        IPage<NoticeEntity> entityPage = iNoticeService.page(page, queryWrapper);
        Map<Long, String> destinationNameMap = loadDestinationNameMap(entityPage.getRecords());
        List<NoticeVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (NoticeEntity record : entityPage.getRecords()) {
                voList.add(toAdminVo(record, destinationNameMap));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(NoticeSaveBo saveBo) {
        validateCategory(saveBo.getCategory());

        NoticeEntity entity = new NoticeEntity();
        BeanUtils.copyProperties(saveBo, entity);
        entity.setDestinationId(publishDestinationResolver.tryFromCoordinates(saveBo.getLatitude(), saveBo.getLongitude()));
        fillDefaults(entity);
        validatePinRequiresPublished(entity.getState(), entity.getPinned());
        clearOtherPinned(entity.getPinned(), null);
        iNoticeService.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(NoticeUpdateBo updateBo) {
        NoticeEntity existing = iNoticeService.getById(updateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.NOTICE_NOT_FOUND);
        }
        validateCategory(updateBo.getCategory());

        validatePinRequiresPublished(updateBo.getState(), updateBo.getPinned());
        clearOtherPinned(updateBo.getPinned(), updateBo.getId());

        NoticeEntity entity = new NoticeEntity();
        BeanUtils.copyProperties(updateBo, entity);
        entity.setDestinationId(publishDestinationResolver.tryFromCoordinates(updateBo.getLatitude(), updateBo.getLongitude()));
        iNoticeService.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(NoticeInfoBo infoBo) {
        NoticeEntity existing = iNoticeService.getById(infoBo.getId());
        if (existing == null) {
            throw new BizException(AppError.NOTICE_NOT_FOUND);
        }
        iNoticeService.removeById(infoBo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateState(NoticeUpdateStateBo updateStateBo) {
        NoticeEntity existing = iNoticeService.getById(updateStateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.NOTICE_NOT_FOUND);
        }
        LambdaUpdateWrapper<NoticeEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(NoticeEntity::getId, updateStateBo.getId())
                .set(NoticeEntity::getState, updateStateBo.getState());
        if (!Objects.equals(updateStateBo.getState(), PUBLISHED)) {
            updateWrapper.set(NoticeEntity::getPinned, 0);
        } else if (existing.getPublishTime() == null) {
            updateWrapper.set(NoticeEntity::getPublishTime, LocalDateTime.now());
        }
        iNoticeService.update(new NoticeEntity(), updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePinned(NoticeUpdatePinnedBo updatePinnedBo) {
        NoticeEntity existing = iNoticeService.getById(updatePinnedBo.getId());
        if (existing == null) {
            throw new BizException(AppError.NOTICE_NOT_FOUND);
        }
        Integer pinned = normalizePinned(updatePinnedBo.getPinned());
        if (Objects.equals(existing.getPinned(), pinned)) {
            return;
        }
        validatePinRequiresPublished(existing.getState(), pinned);
        clearOtherPinned(pinned, existing.getId());

        LambdaUpdateWrapper<NoticeEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(NoticeEntity::getId, updatePinnedBo.getId())
                .set(NoticeEntity::getPinned, pinned);
        iNoticeService.update(new NoticeEntity(), updateWrapper);
    }

    private LambdaQueryWrapper<NoticeEntity> buildPublishedQuery() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<NoticeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoticeEntity::getState, PUBLISHED)
                .le(NoticeEntity::getPublishTime, now)
                .and(wrapper -> wrapper.isNull(NoticeEntity::getExpireTime)
                        .or()
                        .gt(NoticeEntity::getExpireTime, now));
        return queryWrapper;
    }

    private void applyDestinationFilter(LambdaQueryWrapper<NoticeEntity> queryWrapper, Long destinationId) {
        if (destinationId == null) {
            return;
        }
        queryWrapper.and(wrapper -> wrapper.isNull(NoticeEntity::getDestinationId)
                .or()
                .eq(NoticeEntity::getDestinationId, destinationId));
    }

    private void applyAppOrder(LambdaQueryWrapper<NoticeEntity> queryWrapper) {
        queryWrapper.orderByDesc(NoticeEntity::getPinned)
                .orderByDesc(NoticeEntity::getPublishTime)
                .orderByAsc(NoticeEntity::getSort)
                .orderByDesc(NoticeEntity::getId);
    }

    private boolean isPublishedVisible(NoticeEntity entity) {
        if (!Objects.equals(entity.getState(), PUBLISHED)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (entity.getPublishTime() != null && entity.getPublishTime().isAfter(now)) {
            return false;
        }
        return entity.getExpireTime() == null || entity.getExpireTime().isAfter(now);
    }

    private void validateCategory(String category) {
        if (!NoticeCategory.isValid(category)) {
            throw new BizException(AppError.NOTICE_CATEGORY_INVALID);
        }
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

    private void fillDefaults(NoticeEntity entity) {
        entity.setPinned(normalizePinned(entity.getPinned()));
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
    }

    private Integer normalizePinned(Integer pinned) {
        return Objects.equals(pinned, PINNED) ? PINNED : 0;
    }

    private void validatePinRequiresPublished(Integer state, Integer pinned) {
        if (Objects.equals(pinned, PINNED) && !Objects.equals(state, PUBLISHED)) {
            throw new BizException(AppError.NOTICE_PIN_REQUIRES_PUBLISHED);
        }
    }

    private void clearOtherPinned(Integer pinned, Long keepId) {
        if (!Objects.equals(pinned, PINNED)) {
            return;
        }
        LambdaUpdateWrapper<NoticeEntity> clearWrapper = new LambdaUpdateWrapper<>();
        clearWrapper.eq(NoticeEntity::getPinned, PINNED)
                .ne(keepId != null, NoticeEntity::getId, keepId)
                .set(NoticeEntity::getPinned, 0);
        iNoticeService.update(new NoticeEntity(), clearWrapper);
    }

    private Map<Long, String> loadDestinationNameMap(List<NoticeEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new HashMap<>();
        }
        List<Long> destinationIds = records.stream()
                .map(NoticeEntity::getDestinationId)
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

    private NoticeAppVo toAppVo(NoticeEntity entity, boolean includeContent) {
        NoticeAppVo vo = new NoticeAppVo();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setSummary(entity.getSummary());
        if (includeContent) {
            vo.setContent(entity.getContent());
        }
        vo.setTime(entity.getPublishTime() == null ? "" : entity.getPublishTime().format(DATE_FORMAT));
        vo.setCategory(entity.getCategory());
        vo.setCategoryLabel(NoticeCategory.labelOf(entity.getCategory()));
        vo.setSource(entity.getSource());
        vo.setPinned(Objects.equals(entity.getPinned(), 1));
        vo.setIsNew(isNewNotice(entity.getPublishTime()));
        return vo;
    }

    private NoticeVo toAdminVo(NoticeEntity entity, Map<Long, String> destinationNameMap) {
        NoticeVo vo = new NoticeVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setCategoryLabel(NoticeCategory.labelOf(entity.getCategory()));
        vo.setStateLabel(NoticeState.labelOf(entity.getState()));
        if (entity.getDestinationId() != null) {
            vo.setDestinationName(DestinationDisplaySupport.resolveLabel(
                    destinationNameMap, entity.getDestinationId()));
        }
        return vo;
    }

    private boolean isNewNotice(LocalDateTime publishTime) {
        if (publishTime == null) {
            return false;
        }
        return publishTime.isAfter(LocalDateTime.now().minusDays(NEW_DAYS));
    }

    private String buildMarqueeText(List<NoticeAppVo> items) {
        if (CollectionUtils.isEmpty(items)) {
            return "";
        }
        return items.stream()
                .map(item -> {
                    if (StringUtils.isNotBlank(item.getSummary())) {
                        return item.getTitle() + "：" + item.getSummary();
                    }
                    return item.getTitle();
                })
                .collect(Collectors.joining("  "));
    }
}

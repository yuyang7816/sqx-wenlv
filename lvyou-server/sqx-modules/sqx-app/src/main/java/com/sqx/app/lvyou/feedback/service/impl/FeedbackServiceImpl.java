package com.sqx.app.lvyou.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.feedback.db.service.IFeedbackService;
import com.sqx.app.lvyou.feedback.domain.bo.FeedbackPageBo;
import com.sqx.app.lvyou.feedback.domain.bo.FeedbackProcessBo;
import com.sqx.app.lvyou.feedback.domain.bo.FeedbackSubmitBo;
import com.sqx.app.lvyou.feedback.domain.entity.FeedbackEntity;
import com.sqx.app.lvyou.feedback.domain.vo.FeedbackSubmitVo;
import com.sqx.app.lvyou.feedback.domain.vo.FeedbackVo;
import com.sqx.app.lvyou.feedback.enums.FeedbackStatus;
import com.sqx.app.lvyou.feedback.enums.FeedbackType;
import com.sqx.app.lvyou.feedback.service.FeedbackService;
import com.sqx.app.lvyou.feedback.util.FeedbackJsonUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
 * 意见反馈业务服务实现
 */
@Service
public class FeedbackServiceImpl implements FeedbackService {

    private static final int MAX_IMAGES = 3;

    @Resource
    private IFeedbackService iFeedbackService;

    @Resource
    private IUserService iUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeedbackSubmitVo submit(Long userId, FeedbackSubmitBo submitBo) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        String type = StringUtils.trimToEmpty(submitBo.getType());
        if (!FeedbackType.isValid(type)) {
            throw new BizException(AppError.FEEDBACK_TYPE_INVALID);
        }

        List<String> images = normalizeImages(submitBo.getImages());
        if (images.size() > MAX_IMAGES) {
            throw new BizException(AppError.FEEDBACK_IMAGES_LIMIT);
        }

        FeedbackEntity entity = new FeedbackEntity();
        entity.setUserId(userId);
        entity.setType(type);
        entity.setContent(StringUtils.trim(submitBo.getContent()));
        entity.setImages(FeedbackJsonUtil.toJson(images));
        entity.setContactPhone(StringUtils.trim(submitBo.getPhone()));
        entity.setStatus(FeedbackStatus.PENDING.getCode());
        iFeedbackService.save(entity);

        FeedbackSubmitVo vo = new FeedbackSubmitVo();
        vo.setId(entity.getId());
        vo.setStatus(entity.getStatus());
        vo.setStatusLabel(FeedbackStatus.labelOf(entity.getStatus()));
        return vo;
    }

    @Override
    public IPage<FeedbackVo> pageInfo(FeedbackPageBo pageBo) {
        Set<Long> userIdFilter = resolveUserIdFilter(pageBo.getUserPhone());

        Page<FeedbackEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<FeedbackEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(pageBo.getType()), FeedbackEntity::getType, pageBo.getType())
                .eq(StringUtils.isNotBlank(pageBo.getStatus()), FeedbackEntity::getStatus, pageBo.getStatus())
                .like(StringUtils.isNotBlank(pageBo.getContactPhone()), FeedbackEntity::getContactPhone, pageBo.getContactPhone())
                .in(userIdFilter != null, FeedbackEntity::getUserId, userIdFilter)
                .orderByDesc(FeedbackEntity::getCreateTime)
                .orderByDesc(FeedbackEntity::getId);

        IPage<FeedbackEntity> entityPage = iFeedbackService.page(page, queryWrapper);
        Map<Long, UserEntity> userMap = loadUserMap(entityPage.getRecords());
        List<FeedbackVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (FeedbackEntity record : entityPage.getRecords()) {
                voList.add(toVo(record, userMap.get(record.getUserId())));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(FeedbackProcessBo processBo) {
        FeedbackEntity entity = iFeedbackService.getById(processBo.getId());
        if (entity == null) {
            throw new BizException(AppError.FEEDBACK_NOT_FOUND);
        }
        if (FeedbackStatus.PROCESSED.getCode().equals(entity.getStatus())) {
            return;
        }
        entity.setStatus(FeedbackStatus.PROCESSED.getCode());
        iFeedbackService.updateById(entity);
    }

    private List<String> normalizeImages(List<String> images) {
        if (CollectionUtils.isEmpty(images)) {
            return Collections.emptyList();
        }
        return images.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toList());
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

    private Map<Long, UserEntity> loadUserMap(List<FeedbackEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        Set<Long> userIds = new HashSet<>();
        for (FeedbackEntity record : records) {
            if (record.getUserId() != null) {
                userIds.add(record.getUserId());
            }
        }
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserEntity> users = iUserService.listByIds(userIds);
        Map<Long, UserEntity> userMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(users)) {
            for (UserEntity user : users) {
                userMap.put(user.getUserId(), user);
            }
        }
        return userMap;
    }

    private FeedbackVo toVo(FeedbackEntity entity, UserEntity user) {
        FeedbackVo vo = new FeedbackVo();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        if (user != null) {
            vo.setUserName(user.getUserName());
            vo.setUserPhone(user.getPhone());
        }
        vo.setType(entity.getType());
        vo.setContent(entity.getContent());
        vo.setImages(FeedbackJsonUtil.parseList(entity.getImages()));
        vo.setContactPhone(entity.getContactPhone());
        vo.setStatus(entity.getStatus());
        vo.setStatusLabel(FeedbackStatus.labelOf(entity.getStatus()));
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}

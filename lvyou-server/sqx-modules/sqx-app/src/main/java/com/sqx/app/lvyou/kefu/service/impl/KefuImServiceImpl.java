package com.sqx.app.lvyou.kefu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.kefu.db.service.IKefuMessageService;
import com.sqx.app.lvyou.kefu.db.service.IKefuSessionService;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImMessagePageBo;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImReplyBo;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImSendBo;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImSessionPageBo;
import com.sqx.app.lvyou.kefu.domain.entity.KefuMessageEntity;
import com.sqx.app.lvyou.kefu.domain.entity.KefuSessionEntity;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImMessageVo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImSendVo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImSessionVo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImUnreadVo;
import com.sqx.app.lvyou.kefu.enums.KefuImSenderType;
import com.sqx.app.lvyou.kefu.enums.KefuImSessionStatus;
import com.sqx.app.lvyou.kefu.service.KefuImService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KefuImServiceImpl implements KefuImService {

    private static final int DEFAULT_MESSAGE_LIMIT = 100;

    @Resource
    private IKefuSessionService iKefuSessionService;

    @Resource
    private IKefuMessageService iKefuMessageService;

    @Resource
    private IUserService iUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KefuImSessionVo openSession(Long userId) {
        UserEntity user = requireUser(userId);
        KefuSessionEntity session = getOrCreateOpenSession(userId);
        return toSessionVo(session, user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KefuImSendVo sendUserMessage(Long userId, KefuImSendBo sendBo) {
        requireUser(userId);
        KefuSessionEntity session = getOrCreateOpenSession(userId);
        if (KefuImSessionStatus.CLOSED.getCode().equals(session.getStatus())) {
            session.setStatus(KefuImSessionStatus.OPEN.getCode());
            iKefuSessionService.updateById(session);
        }
        KefuMessageEntity message = saveMessage(session.getId(), KefuImSenderType.USER.getCode(), userId, sendBo.getContent());
        bumpSessionAfterMessage(session, message, true);
        KefuImSendVo vo = new KefuImSendVo();
        vo.setMessageId(message.getId());
        vo.setSessionId(session.getId());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<KefuImMessageVo> listUserMessages(Long userId, KefuImMessagePageBo pageBo) {
        KefuSessionEntity session = getOrCreateOpenSession(userId);
        clearUserUnread(session.getId());
        return listMessages(session.getId(), pageBo.getSinceId(), resolveLimit(pageBo.getLimit()));
    }

    @Override
    public IPage<KefuImSessionVo> adminSessionPage(KefuImSessionPageBo pageBo) {
        Set<Long> userIdFilter = resolveUserIdFilter(pageBo.getUserPhone());
        if (userIdFilter != null && userIdFilter.isEmpty()) {
            return PageUtil.copyPage(new Page<>(pageBo.getPage(), pageBo.getLimit()), Collections.emptyList());
        }

        Page<KefuSessionEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<KefuSessionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(pageBo.getStatus()), KefuSessionEntity::getStatus, pageBo.getStatus())
                .gt(Boolean.TRUE.equals(pageBo.getUnreadOnly()), KefuSessionEntity::getAdminUnreadCount, 0)
                .in(userIdFilter != null, KefuSessionEntity::getUserId, userIdFilter)
                .orderByDesc(KefuSessionEntity::getLastMessageTime)
                .orderByDesc(KefuSessionEntity::getId);

        IPage<KefuSessionEntity> entityPage = iKefuSessionService.page(page, queryWrapper);
        Map<Long, UserEntity> userMap = loadUserMap(entityPage.getRecords());
        List<KefuImSessionVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (KefuSessionEntity record : entityPage.getRecords()) {
                voList.add(toSessionVo(record, userMap.get(record.getUserId())));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<KefuImMessageVo> adminListMessages(KefuImMessagePageBo pageBo) {
        KefuSessionEntity session = requireSession(pageBo.getSessionId());
        clearAdminUnread(session.getId());
        return listMessages(session.getId(), pageBo.getSinceId(), resolveLimit(pageBo.getLimit()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KefuImSendVo adminReply(Long staffId, KefuImReplyBo replyBo) {
        KefuSessionEntity session = requireSession(replyBo.getSessionId());
        if (KefuImSessionStatus.CLOSED.getCode().equals(session.getStatus())) {
            throw new BizException(AppError.KEFU_IM_SESSION_CLOSED);
        }
        KefuMessageEntity message = saveMessage(session.getId(), KefuImSenderType.STAFF.getCode(), staffId, replyBo.getContent());
        bumpSessionAfterMessage(session, message, false);
        KefuImSendVo vo = new KefuImSendVo();
        vo.setMessageId(message.getId());
        vo.setSessionId(session.getId());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminCloseSession(Long sessionId) {
        KefuSessionEntity session = requireSession(sessionId);
        session.setStatus(KefuImSessionStatus.CLOSED.getCode());
        iKefuSessionService.updateById(session);
    }

    @Override
    public KefuImUnreadVo adminUnreadTotal() {
        LambdaQueryWrapper<KefuSessionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KefuSessionEntity::getStatus, KefuImSessionStatus.OPEN.getCode())
                .gt(KefuSessionEntity::getAdminUnreadCount, 0);
        List<KefuSessionEntity> sessions = iKefuSessionService.list(queryWrapper);
        int total = 0;
        if (CollectionUtils.isNotEmpty(sessions)) {
            for (KefuSessionEntity session : sessions) {
                total += safeCount(session.getAdminUnreadCount());
            }
        }
        KefuImUnreadVo vo = new KefuImUnreadVo();
        vo.setCount(total);
        return vo;
    }

    private UserEntity requireUser(Long userId) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        return user;
    }

    private KefuSessionEntity requireSession(Long sessionId) {
        if (sessionId == null) {
            throw new BizException(AppError.KEFU_IM_SESSION_NOT_FOUND);
        }
        KefuSessionEntity session = iKefuSessionService.getById(sessionId);
        if (session == null) {
            throw new BizException(AppError.KEFU_IM_SESSION_NOT_FOUND);
        }
        return session;
    }

    private KefuSessionEntity getOrCreateOpenSession(Long userId) {
        LambdaQueryWrapper<KefuSessionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KefuSessionEntity::getUserId, userId)
                .eq(KefuSessionEntity::getStatus, KefuImSessionStatus.OPEN.getCode())
                .orderByDesc(KefuSessionEntity::getId)
                .last("LIMIT 1");
        KefuSessionEntity session = iKefuSessionService.getOne(queryWrapper, false);
        if (session != null) {
            return session;
        }

        session = new KefuSessionEntity();
        session.setUserId(userId);
        session.setStatus(KefuImSessionStatus.OPEN.getCode());
        session.setAdminUnreadCount(0);
        session.setUserUnreadCount(0);
        iKefuSessionService.save(session);
        return session;
    }

    private KefuMessageEntity saveMessage(Long sessionId, String senderType, Long senderId, String content) {
        KefuMessageEntity message = new KefuMessageEntity();
        message.setSessionId(sessionId);
        message.setSenderType(senderType);
        message.setSenderId(senderId);
        message.setContent(StringUtils.trim(content));
        iKefuMessageService.save(message);
        return message;
    }

    private void bumpSessionAfterMessage(KefuSessionEntity session, KefuMessageEntity message, boolean fromUser) {
        session.setLastMessage(trimPreview(message.getContent()));
        session.setLastMessageTime(message.getCreateTime() != null ? message.getCreateTime() : LocalDateTime.now());
        if (fromUser) {
            session.setAdminUnreadCount(safeCount(session.getAdminUnreadCount()) + 1);
        } else {
            session.setUserUnreadCount(safeCount(session.getUserUnreadCount()) + 1);
        }
        iKefuSessionService.updateById(session);
    }

    private void clearUserUnread(Long sessionId) {
        LambdaUpdateWrapper<KefuSessionEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(KefuSessionEntity::getId, sessionId)
                .set(KefuSessionEntity::getUserUnreadCount, 0);
        iKefuSessionService.update(updateWrapper);
    }

    private void clearAdminUnread(Long sessionId) {
        LambdaUpdateWrapper<KefuSessionEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(KefuSessionEntity::getId, sessionId)
                .set(KefuSessionEntity::getAdminUnreadCount, 0);
        iKefuSessionService.update(updateWrapper);
    }

    private List<KefuImMessageVo> listMessages(Long sessionId, Long sinceId, int limit) {
        LambdaQueryWrapper<KefuMessageEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KefuMessageEntity::getSessionId, sessionId)
                .gt(sinceId != null && sinceId > 0, KefuMessageEntity::getId, sinceId)
                .orderByAsc(KefuMessageEntity::getId)
                .last("LIMIT " + limit);
        List<KefuMessageEntity> records = iKefuMessageService.list(queryWrapper);
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        List<KefuImMessageVo> voList = new ArrayList<>(records.size());
        for (KefuMessageEntity record : records) {
            voList.add(toMessageVo(record));
        }
        return voList;
    }

    private int resolveLimit(Long limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_MESSAGE_LIMIT;
        }
        return (int) Math.min(limit, DEFAULT_MESSAGE_LIMIT);
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private String trimPreview(String content) {
        String text = StringUtils.defaultString(content);
        if (text.length() <= 80) {
            return text;
        }
        return text.substring(0, 80) + "...";
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

    private Map<Long, UserEntity> loadUserMap(List<KefuSessionEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        Set<Long> userIds = new HashSet<>();
        for (KefuSessionEntity record : records) {
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

    private KefuImSessionVo toSessionVo(KefuSessionEntity entity, UserEntity user) {
        KefuImSessionVo vo = new KefuImSessionVo();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        if (user != null) {
            vo.setUserName(user.getUserName());
            vo.setUserPhone(user.getPhone());
        }
        vo.setStatus(entity.getStatus());
        vo.setStatusLabel(KefuImSessionStatus.labelOf(entity.getStatus()));
        vo.setLastMessage(entity.getLastMessage());
        vo.setLastMessageTime(entity.getLastMessageTime());
        vo.setAdminUnreadCount(safeCount(entity.getAdminUnreadCount()));
        vo.setUserUnreadCount(safeCount(entity.getUserUnreadCount()));
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private KefuImMessageVo toMessageVo(KefuMessageEntity entity) {
        KefuImMessageVo vo = new KefuImMessageVo();
        vo.setId(entity.getId());
        vo.setSessionId(entity.getSessionId());
        vo.setSenderType(entity.getSenderType());
        vo.setSenderTypeLabel(KefuImSenderType.labelOf(entity.getSenderType()));
        vo.setSenderId(entity.getSenderId());
        vo.setContent(entity.getContent());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}

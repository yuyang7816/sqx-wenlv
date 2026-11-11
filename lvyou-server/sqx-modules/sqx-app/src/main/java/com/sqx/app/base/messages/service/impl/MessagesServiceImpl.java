package com.sqx.app.base.messages.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.messages.domain.vo.MessagesVo;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.messages.domain.bo.MessagesInfoBo;
import com.sqx.app.base.messages.domain.bo.MessagesPageBo;
import com.sqx.app.base.messages.domain.bo.MessagesSaveBo;
import com.sqx.app.base.messages.domain.bo.MessagesUpdateBo;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.base.messages.service.MessagesService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 消息信息表 服务实现类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-15 16:28:04
 */
@Service
public class MessagesServiceImpl implements MessagesService {

    @Resource
    private IMessagesService iMessagesService;

    @Override
    public void saveInfo(MessagesSaveBo messagesSaveBo) {
        Messages messages = new Messages();
        BeanUtils.copyProperties(messagesSaveBo, messages);

        iMessagesService.save(messages);
    }

    @Override
    public IPage<MessagesVo> pageInfo(Long userId, MessagesPageBo messagesPageBo) {
        messagesPageBo.setUserId(userId);
        Page<Messages> initPage = PageUtil.initPage(messagesPageBo.getPage(), messagesPageBo.getLimit());
        return iMessagesService.pageInfo(initPage, messagesPageBo);
    }

    @Override
    public MessagesVo info(Long userId, MessagesInfoBo messagesInfoBo) {
        Messages messages = requireOwnedMessage(userId, messagesInfoBo.getId());
        MessagesVo vo = new MessagesVo();
        BeanUtils.copyProperties(messages, vo);
        return vo;
    }

    @Override
    public IPage<MessagesVo> adminPageInfo(MessagesPageBo messagesPageBo) {
        Page<Messages> initPage = PageUtil.initPage(messagesPageBo.getPage(), messagesPageBo.getLimit());
        return iMessagesService.pageInfo(initPage, messagesPageBo);
    }

    @Override
    public MessagesVo adminInfo(MessagesInfoBo messagesInfoBo) {
        Messages messages = iMessagesService.getById(messagesInfoBo.getId());
        if (messages == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        MessagesVo vo = new MessagesVo();
        BeanUtils.copyProperties(messages, vo);
        return vo;
    }

    @Override
    public void markRead(Long userId, MessagesInfoBo messagesInfoBo) {
        requireOwnedMessage(userId, messagesInfoBo.getId());
        Messages update = new Messages();
        update.setId(messagesInfoBo.getId());
        update.setIsRead(1);
        iMessagesService.updateById(update);
    }

    @Override
    public int markAllRead(Long userId, Integer type) {
        LambdaUpdateWrapper<Messages> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Messages::getUserId, userId)
                .eq(Messages::getIsRead, 0);
        if (type != null) {
            updateWrapper.eq(Messages::getType, type);
        }
        updateWrapper.set(Messages::getIsRead, 1);
        return iMessagesService.getBaseMapper().update(null, updateWrapper);
    }

    @Override
    public long unreadCount(Long userId) {
        return unreadCount(userId, null);
    }

    @Override
    public long unreadCount(Long userId, Integer type) {
        QueryWrapper<Messages> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("is_read", 0);
        if (type != null) {
            queryWrapper.eq("type", type);
        }
        return iMessagesService.count(queryWrapper);
    }

    private Messages requireOwnedMessage(Long userId, Long messageId) {
        Messages messages = iMessagesService.getById(messageId);
        if (messages == null || !Objects.equals(messages.getUserId(), userId)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        return messages;
    }

    @Override
    public void updateInfo(MessagesUpdateBo messagesUpdateBo) {
        Messages messages = new Messages();
        BeanUtils.copyProperties(messagesUpdateBo, messages);

        iMessagesService.updateById(messages);
    }

    @Override
    public void delete(MessagesInfoBo messagesInfoBo) {
        iMessagesService.removeById(messagesInfoBo.getId());
    }
}

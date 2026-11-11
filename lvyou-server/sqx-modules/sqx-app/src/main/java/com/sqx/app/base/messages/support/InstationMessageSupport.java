package com.sqx.app.base.messages.support;

import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 站内消息写入辅助（订单/预约等业务状态变更时调用）
 */
@Component
public class InstationMessageSupport {

    private static final int TYPE_USER_MESSAGE = 2;

    @Resource
    private INotificationService notificationService;

    /**
     * 向指定用户写入一条站内消息
     */
    public void sendUserMessage(Long userId, String title, String content, Long dataId) {
        if (userId == null) {
            return;
        }
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setTitle(title);
        request.setContent(content);
        request.setType(TYPE_USER_MESSAGE);
        request.setDataId(dataId);
        notificationService.send(request, NotificationChannel.INSTATION.code());
    }
}

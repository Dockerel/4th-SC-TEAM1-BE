package com.gdg.Todak.notification.facade;

import com.gdg.Todak.notification.dto.PublishNotificationRequest;
import com.gdg.Todak.notification.entity.Notification;
import com.gdg.Todak.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationService notificationService;

    public void publishNotification(PublishNotificationRequest request) {
        Notification.checkNotificationSenderAndReceiver(request);

        notificationService.saveNotification(request);

        notificationService.publishNotification(Notification.from(request));
    }
}

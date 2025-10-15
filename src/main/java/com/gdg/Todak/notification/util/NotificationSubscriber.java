package com.gdg.Todak.notification.util;

import com.gdg.Todak.notification.config.RabbitMQConfig;
import com.gdg.Todak.notification.dto.PublishNotificationRequest;
import com.gdg.Todak.notification.entity.Notification;
import com.gdg.Todak.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationSubscriber {

    private final RabbitTemplate rabbitTemplate;

    private final NotificationService notificationService;
    private final SlackPublisher slackPublisher;

    @RabbitListener(queues = RabbitMQConfig.SAVE_NOTIFICATION_QUEUE)
    public void consumeSaveNotificationMessage(PublishNotificationRequest request) {
        // 알림 저장
        Notification notification = notificationService.saveNotification(request);

        // 알림 저장 후 알림 발행
        rabbitTemplate.convertAndSend(RabbitMQConfig.PUBLISH_NOTIFICATION_EXCHANGE, "", notification);
    }

    @RabbitListener(queues = "#{@dynamicPublishNotificationQueueName}")
    public void consumePublishNotificationMessage(Notification notification) {
        notificationService.publishNotification(notification);
    }

    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
    public void consumeDeadLetterNotificationMessage(PublishNotificationRequest request) {
        slackPublisher.publishSlackMessage(request);
    }
}

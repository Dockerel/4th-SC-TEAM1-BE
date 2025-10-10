package com.gdg.Todak.notification.util;

import com.gdg.Todak.notification.config.RabbitMQConfig;
import com.gdg.Todak.notification.dto.PublishNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishSaveNotificationMessage(PublishNotificationRequest request) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.SAVE_NOTIFICATION_QUEUE, RabbitMQConfig.SAVE_NOTIFICATION_QUEUE, request);
    }
}

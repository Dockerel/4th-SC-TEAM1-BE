package com.gdg.Todak.notification.service;

import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.notification.dto.PublishNotificationRequest;
import com.gdg.Todak.notification.entity.Notification;
import com.gdg.Todak.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static com.gdg.Todak.common.exception.errors.NotificationError.NOTIFICATION_NOT_FOUND_ERROR;
import static com.gdg.Todak.common.exception.errors.NotificationError.NOT_NOTIFICATION_OWNER_ERROR;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final SseService sseService;

    private final NotificationRepository notificationRepository;

    public List<Notification> getStoredMessages(String userId) {
        return notificationRepository.findAllByReceiverUserId(userId);
    }

    @Transactional
    public Notification saveNotification(PublishNotificationRequest request) {
        Notification notification = Notification.from(request);
        notificationRepository.save(notification);
        return notification;
    }

    @Transactional
    @Async(value = "steadyExecutor")
    public void deleteAckNotification(String userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new TodakException(NOTIFICATION_NOT_FOUND_ERROR));
        if (notification.getReceiverUserId() != userId) {
            throw new TodakException(NOT_NOTIFICATION_OWNER_ERROR);
        }
        notificationRepository.delete(notification);
    }

    @Async(value = "steadyExecutor")
    public void publishNotification(Notification notification) {
        sendNotificationToEmitters(notification);
    }

    private void sendNotificationToEmitters(Notification notification) {
        List<SseEmitter> emitters = sseService.getEmitters(notification.getReceiverUserId());
        String receiverUserId = notification.getReceiverUserId();

        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("notification")
                                .data(notification)
                );
                log.info("Sent SSE to user: {} with notification: {} at time: {}", receiverUserId, notification, Instant.now());
            } catch (IOException e) {
                log.error("Error sending SSE to user: {} with message: {}", receiverUserId, e.getMessage());
                emitters.remove(emitter);
            }
        }
    }
}

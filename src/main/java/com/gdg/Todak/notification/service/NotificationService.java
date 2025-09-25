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
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.gdg.Todak.common.exception.errors.NotificationError.NOTIFICATION_NOT_FOUND_ERROR;
import static com.gdg.Todak.common.exception.errors.NotificationError.NOT_NOTIFICATION_OWNER_ERROR;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final RedisSubscriberService redisSubscriberService;
    private final RedisPublisherService redisPublisherService;
    private final NotificationRepository notificationRepository;

    public SseEmitter createEmitter(String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        redisSubscriberService.addEmitter(userId, emitter);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> sendHeartbeat(userId, emitter, executor), 0, 15, TimeUnit.SECONDS);

        setEmitterCallbacks(userId, emitter, executor);
        return emitter;
    }

    public List<Notification> getStoredMessages(String userId) {
        return notificationRepository.findAllByReceiverUserId(userId);
    }

    private void sendHeartbeat(String userId, SseEmitter emitter, ScheduledExecutorService executor) {
        try {
            emitter.send(SseEmitter.event()
                    .name("heartbeat")
                    .data("heartbeat"));
        } catch (IOException e) {
            log.warn("Error sending heartbeat, connection might be closed. Removing emitter and shutting down executor.", e);
            redisSubscriberService.removeEmitter(userId, emitter);
            emitter.completeWithError(e);
            executor.shutdown();
        }
    }

    private void setEmitterCallbacks(String userId, SseEmitter emitter, ScheduledExecutorService executor) {
        emitter.onCompletion(() -> {
            redisSubscriberService.removeEmitter(userId, emitter);
            log.info("Emitter completed for user: {}", userId);
            executor.shutdown();
        });

        emitter.onTimeout(() -> {
            redisSubscriberService.removeEmitter(userId, emitter);
            log.info("Emitter timed out for user: {}", userId);
            executor.shutdown();
        });

        emitter.onError((Throwable t) -> {
            redisSubscriberService.removeEmitter(userId, emitter);
            log.error("Emitter error for user: {}", userId, t);
            executor.shutdown();
        });
    }

    @Async(value = "steadyExecutor")
    @Transactional
    public void saveNotification(PublishNotificationRequest request) {
        Notification notification = Notification.from(request);
        notificationRepository.save(notification);
    }

    public void publishEventToRedis(Notification notification) {
        redisPublisherService.publish("notification", notification);
    }

    public String deleteAckNotification(String userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new TodakException(NOTIFICATION_NOT_FOUND_ERROR));
        if (notification.getReceiverUserId() != userId) {
            throw new TodakException(NOT_NOTIFICATION_OWNER_ERROR);
        }
        notificationRepository.delete(notification);
        return "notification deleted";
    }
}

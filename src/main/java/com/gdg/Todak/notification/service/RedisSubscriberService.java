package com.gdg.Todak.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static com.gdg.Todak.common.exception.errors.NotificationError.NOTIFICATION_CREATION_ERROR;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriberService {

    private final SseService sseService;

    private final ObjectMapper objectMapper;

    @Async(value = "steadyExecutor")
    public void onMessage(String channel, String message) {
        log.info("Received message from channel: [{}] at time: {} with message: {}", channel, Instant.now(), message);
        processMessage(message);
    }

    private void processMessage(String message) {
        try {
            Notification notification = objectMapper.readValue(message, Notification.class);
            sendNotificationToEmitters(notification);
        } catch (JsonProcessingException e) {
            log.info("Error while parsing json message: {}", e.getMessage());
            throw new TodakException(NOTIFICATION_CREATION_ERROR);
        }
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

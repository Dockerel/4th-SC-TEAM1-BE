package com.gdg.Todak.notification.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class SseService {

    public static final int SSE_HEARTBEAT_PERIOD = 15;

    private ConcurrentMap<String, List<SseEmitter>> sseConnections;

    private ScheduledExecutorService scheduledExecutorService;

    @PostConstruct
    public void init() {
        this.sseConnections = new ConcurrentHashMap<>();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(10);
    }

    public SseEmitter createEmitter(String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        sseConnections.computeIfAbsent(userId, k -> new ArrayList<>()).add(emitter);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> sendHeartbeat(userId, emitter, executor), 0, SSE_HEARTBEAT_PERIOD, TimeUnit.SECONDS);

        setEmitterCallbacks(userId, emitter, executor);
        return emitter;
    }

    private void sendHeartbeat(String userId, SseEmitter emitter, ScheduledExecutorService executor) {
        try {
            emitter.send(SseEmitter.event()
                    .name("heartbeat")
                    .data("heartbeat"));
        } catch (IOException e) {
            log.warn("Error sending heartbeat, connection might be closed. Removing emitter and shutting down executor.", e);
            removeEmitter(userId, emitter);
            emitter.completeWithError(e);
            executor.shutdown();
        }
    }

    private void removeEmitter(String userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = sseConnections.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                sseConnections.remove(userId);
            }
        }
        log.info("Emitter removed for user: {}", userId);
    }

    private void setEmitterCallbacks(String userId, SseEmitter emitter, ScheduledExecutorService executor) {
        emitter.onCompletion(() -> {
            removeEmitter(userId, emitter);
            log.info("Emitter completed for user: {}", userId);
            executor.shutdown();
        });

        emitter.onTimeout(() -> {
            removeEmitter(userId, emitter);
            log.info("Emitter timed out for user: {}", userId);
            executor.shutdown();
        });

        emitter.onError((Throwable t) -> {
            removeEmitter(userId, emitter);
            log.error("Emitter error for user: {}", userId, t);
            executor.shutdown();
        });
    }

    public List<SseEmitter> getEmitters(String userId) {
        return sseConnections.get(userId);
    }
}

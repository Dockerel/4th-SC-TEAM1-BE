package com.gdg.Todak.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPublisherService {

    private final RedisTemplate redisTemplate;

    public void publish(String channel, Object message) {
        log.info("Publishing message to channel: [{}] at time: {} with message: {}", channel, Instant.now(), message);
        redisTemplate.convertAndSend(channel, message);
        log.info("Published message to channel: [{}] at time: {} with message: {}", channel, Instant.now(), message);
    }

}

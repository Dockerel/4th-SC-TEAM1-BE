package com.gdg.Todak.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class PublishNotificationRequest {
    private String senderId;
    private String receiverId;
    private String type;
    private Long objectId;
    private Instant createdAt;

    public static PublishNotificationRequest of(String senderId, String receiverId, String type, Long objectId, Instant createdAt) {
        return PublishNotificationRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .type(type)
                .objectId(objectId)
                .createdAt(createdAt)
                .build();
    }
}

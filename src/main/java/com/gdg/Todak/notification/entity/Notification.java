package com.gdg.Todak.notification.entity;

import com.gdg.Todak.notification.dto.PublishNotificationRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@Getter
@ToString
@NoArgsConstructor
public class Notification {
    private Long id;
    private Long objectId;
    private String senderUserId;
    private String receiverUserId;
    private String type;
    private Instant createdAt;

    @Builder
    public Notification(Long objectId, String senderUserId, String receiverUserId, String type, Instant createdAt) {
        this.objectId = objectId;
        this.senderUserId = senderUserId;
        this.receiverUserId = receiverUserId;
        this.type = type;
        this.createdAt = createdAt;
    }

    public static Notification from(PublishNotificationRequest request) {
        return Notification.builder()
                .objectId(request.getObjectId())
                .senderUserId(request.getSenderId())
                .receiverUserId(request.getReceiverId())
                .type(request.getType())
                .createdAt(request.getCreatedAt())
                .build();
    }

    public static void checkNotificationSenderAndReceiver(PublishNotificationRequest request) {
        String senderId = request.getSenderId();
        String receiverId = request.getReceiverId();

        if (senderId == receiverId) {
            throw new RuntimeException("Sender and Receiver can't be same");
        }
    }
}

package com.gdg.Todak.notification.entity;

import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.notification.dto.PublishNotificationRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

import static com.gdg.Todak.common.exception.errors.NotificationError.SENDER_AND_RECEIVER_SAME_ERROR;

@Getter
@ToString
@NoArgsConstructor
@Entity
public class Notification {
    @Id
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
            throw new TodakException(SENDER_AND_RECEIVER_SAME_ERROR);
        }
    }
}

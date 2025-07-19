package com.gdg.Todak.event.listener;

import com.gdg.Todak.diary.entity.Comment;
import com.gdg.Todak.event.event.NewCommentEvent;
import com.gdg.Todak.notification.dto.PublishNotificationRequest;
import com.gdg.Todak.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;


@Component
@RequiredArgsConstructor
public class NewCommentNotificationListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleCommentSaved(NewCommentEvent event) {
        Comment comment = event.getComment();
        String senderId = comment.getMember().getUserId();
        String receiverId = comment.getDiary().getMember().getUserId();
        Long diaryId = comment.getDiary().getId();
        Instant createdAt = comment.getCreatedAt();

        PublishNotificationRequest request = PublishNotificationRequest.of(senderId, receiverId, "comment", diaryId, createdAt);

        notificationService.publishNotification(request);
    }
}

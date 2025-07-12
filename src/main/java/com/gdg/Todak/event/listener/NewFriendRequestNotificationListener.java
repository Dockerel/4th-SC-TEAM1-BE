package com.gdg.Todak.event.listener;

import com.gdg.Todak.event.event.NewFriendRequestEvent;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.notification.dto.PublishNotificationRequest;
import com.gdg.Todak.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class NewFriendRequestNotificationListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDiarySaved(NewFriendRequestEvent event) {
        Friend friend = event.getFriend();
        String senderId = friend.getRequester().getUserId();
        String receiverId = friend.getAccepter().getUserId();

        PublishNotificationRequest request = PublishNotificationRequest.of(senderId, receiverId, "friend", null, Instant.now());

        notificationService.publishNotification(request);
    }
}

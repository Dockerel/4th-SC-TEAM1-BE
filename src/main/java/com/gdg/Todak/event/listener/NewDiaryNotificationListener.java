package com.gdg.Todak.event.listener;

import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.event.event.NewDiaryEvent;
import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.repository.FriendRepository;
import com.gdg.Todak.notification.dto.PublishNotificationRequest;
import com.gdg.Todak.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NewDiaryNotificationListener {

    private final NotificationService notificationService;
    private final FriendRepository friendRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDiarySaved(NewDiaryEvent event) {
        Diary diary = event.getDiary();
        String senderId = diary.getMember().getUserId();

        List<Friend> friends = friendRepository.findAllByAccepterUserIdAndFriendStatusOrRequesterUserIdAndFriendStatus(
                senderId, FriendStatus.ACCEPTED, // requester
                senderId, FriendStatus.ACCEPTED // acceptor
        );

        for (Friend friend : friends) {
            String receiverId = friend.getFriend(senderId).getUserId();
            PublishNotificationRequest request = PublishNotificationRequest.of(senderId, receiverId, "diary", diary.getId(), diary.getCreatedAt());
            notificationService.publishNotification(request);
        }
    }
}

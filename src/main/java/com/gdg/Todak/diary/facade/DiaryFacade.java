package com.gdg.Todak.diary.facade;

import com.gdg.Todak.diary.dto.DiaryRequest;
import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.diary.service.DiaryService;
import com.gdg.Todak.diary.service.SchedulerService;
import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.repository.FriendRepository;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.notification.dto.PublishNotificationRequest;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointRequest;
import com.gdg.Todak.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class DiaryFacade {

    private final DiaryService diaryService;
    private final PointService pointService;
    private final SchedulerService schedulerService;

    private final ApplicationEventPublisher eventPublisher;

    private final FriendRepository friendRepository;

    @Transactional
    public void writeDiary(String userId, DiaryRequest diaryRequest) {
        // 1. 댓글 저장
        Diary diary = diaryService.writeDiary(userId, diaryRequest);

        // 2. 포인트 적립
        pointService.earnPointByType(new PointRequest(diary.getMember(), PointType.DIARY));

        // 3. AI 댓글 작성 예약
        schedulerService.scheduleSavingCommentByAI(diary);

        // 4. 알림 이벤트 발행
        publishNotification(diary);
    }

    private void publishNotification(Diary diary) {
        String senderId = diary.getMember().getUserId();
        List<Friend> friends = friendRepository.findAllByAccepterUserIdAndFriendStatusOrRequesterUserIdAndFriendStatus(
                senderId, FriendStatus.ACCEPTED, // requester
                senderId, FriendStatus.ACCEPTED // acceptor
        );

        for (Friend friend : friends) {
            Member member = friend.getFriend(senderId);
            PublishNotificationRequest notificationRequest = PublishNotificationRequest.of(
                    senderId,
                    member.getUserId(),
                    "post",
                    diary.getId(),
                    diary.getCreatedAt()
            );
            eventPublisher.publishEvent(notificationRequest);
        }
    }
}

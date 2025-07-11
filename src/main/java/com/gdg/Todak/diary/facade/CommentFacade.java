package com.gdg.Todak.diary.facade;

import com.gdg.Todak.diary.dto.CommentRequest;
import com.gdg.Todak.diary.entity.Comment;
import com.gdg.Todak.diary.service.CommentService;
import com.gdg.Todak.notification.dto.PublishNotificationRequest;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointRequest;
import com.gdg.Todak.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CommentFacade {

    private final CommentService commentService;
    private final PointService pointService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void saveComment(String userId, Long diaryId, CommentRequest request) {
        // 1. 댓글 저장
        Comment comment = commentService.saveComment(userId, diaryId, request);

        // 2. 포인트 적립
        pointService.earnPointByType(new PointRequest(comment.getMember(), PointType.COMMENT));

        // 3. 알림 이벤트 발행
        PublishNotificationRequest notificationRequest = PublishNotificationRequest.of(
                userId,
                comment.getDiary().getMember().getUserId(),
                "comment",
                comment.getDiary().getId(),
                comment.getDiary().getCreatedAt()
        );
        eventPublisher.publishEvent(notificationRequest);
    }
}

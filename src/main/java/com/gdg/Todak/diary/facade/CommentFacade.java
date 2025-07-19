package com.gdg.Todak.diary.facade;

import com.gdg.Todak.diary.dto.CommentRequest;
import com.gdg.Todak.diary.entity.Comment;
import com.gdg.Todak.diary.service.CommentService;
import com.gdg.Todak.event.event.NewCommentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommentFacade {

    private final CommentService commentService;
    private final ApplicationEventPublisher eventPublisher;

    public void saveComment(String userId, Long diaryId, CommentRequest request) {
        Comment comment = commentService.saveComment(userId, diaryId, request);
        eventPublisher.publishEvent(NewCommentEvent.of(comment));
    }
}

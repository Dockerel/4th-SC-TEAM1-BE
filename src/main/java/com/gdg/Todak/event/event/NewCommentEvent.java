package com.gdg.Todak.event.event;

import com.gdg.Todak.diary.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NewCommentEvent {
    private Comment comment;

    public static NewCommentEvent of(Comment comment) {
        return NewCommentEvent.builder()
                .comment(comment)
                .build();
    }
}

package com.gdg.Todak.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class SaveCommentDto {
    private String senderId;
    private String receiverId;
    private Long diaryId;
    private Instant createdAt;

    public static SaveCommentDto of(String senderId, String receiverId, Long diaryId, Instant createdAt) {
        return SaveCommentDto.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .diaryId(diaryId)
                .createdAt(createdAt)
                .build();
    }
}

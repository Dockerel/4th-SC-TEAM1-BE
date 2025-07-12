package com.gdg.Todak.diary.facade;

import com.gdg.Todak.diary.dto.DiaryRequest;
import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.diary.service.DiaryService;
import com.gdg.Todak.event.event.NewDiaryEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class DiaryFacade {

    private final DiaryService diaryService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void writeDiary(String userId, DiaryRequest diaryRequest) {
        Diary diary = diaryService.writeDiary(userId, diaryRequest);
        eventPublisher.publishEvent(NewDiaryEvent.of(diary));
    }
}

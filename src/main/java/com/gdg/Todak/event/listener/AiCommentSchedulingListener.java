package com.gdg.Todak.event.listener;

import com.gdg.Todak.diary.service.SchedulerService;
import com.gdg.Todak.event.event.NewDiaryEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AiCommentSchedulingListener {

    private final SchedulerService schedulerService;

    @Async
    @EventListener
    public void handleDiarySaved(NewDiaryEvent event) {
        schedulerService.scheduleSavingCommentByAI(event.getDiary());
    }
}

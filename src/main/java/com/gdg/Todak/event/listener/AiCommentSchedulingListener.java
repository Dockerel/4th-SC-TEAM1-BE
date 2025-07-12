package com.gdg.Todak.event.listener;

import com.gdg.Todak.diary.service.SchedulerService;
import com.gdg.Todak.event.event.NewDiaryEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class AiCommentSchedulingListener {

    private final SchedulerService schedulerService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDiarySaved(NewDiaryEvent event) {
        schedulerService.scheduleSavingCommentByAI(event.getDiary());
    }
}

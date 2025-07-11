package com.gdg.Todak.event.listener;

import com.gdg.Todak.common.lock.exception.LockException;
import com.gdg.Todak.event.event.NewCommentEvent;
import com.gdg.Todak.event.event.NewDiaryEvent;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointRequest;
import com.gdg.Todak.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PointEarningListener {

    private final PointService pointService;

    @Async
    @Retryable(
            value = {
                    LockException.class,
                    DeadlockLoserDataAccessException.class
            },
            backoff = @Backoff(delay = 1000)
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentSaved(NewCommentEvent event) {
        pointService.earnPointByType(PointRequest.of(event.getComment().getMember(), PointType.COMMENT));
    }

    @Async
    @Retryable(
            value = {
                    LockException.class,
                    DeadlockLoserDataAccessException.class
            },
            backoff = @Backoff(delay = 1000)
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDiarySaved(NewDiaryEvent event) {
        pointService.earnPointByType(PointRequest.of(event.getDiary().getMember(), PointType.DIARY));
    }
}

package com.gdg.Todak.event.listener;

import com.gdg.Todak.common.lock.exception.LockException;
import com.gdg.Todak.event.event.LoginEvent;
import com.gdg.Todak.event.event.NewCommentEvent;
import com.gdg.Todak.event.event.NewDiaryEvent;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointRequest;
import com.gdg.Todak.point.facade.PointFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
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

    public static final int DELAY = 100;
    public static final int MULTIPLIER = 2;

    private final PointFacade pointFacade;

    @Async
    @Retryable(
            value = {
                    LockException.class,
                    DeadlockLoserDataAccessException.class
            },
            backoff = @Backoff(delay = DELAY, multiplier = MULTIPLIER, random = true)
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLogin(LoginEvent event) {
        pointFacade.earnAttendancePointPerDay(event.getMember());
    }

    @Async
    @Retryable(
            value = {
                    LockException.class,
                    DeadlockLoserDataAccessException.class
            },
            backoff = @Backoff(delay = DELAY, multiplier = MULTIPLIER, random = true)
    )
    @EventListener
    public void handleCommentSaved(NewCommentEvent event) {
        pointFacade.earnPointByType(PointRequest.of(event.getComment().getMember(), PointType.COMMENT));
    }

    @Async
    @Retryable(
            value = {
                    LockException.class,
                    DeadlockLoserDataAccessException.class
            },
            backoff = @Backoff(delay = DELAY, multiplier = MULTIPLIER, random = true)
    )
    @EventListener
    public void handleDiarySaved(NewDiaryEvent event) {
        pointFacade.earnPointByType(PointRequest.of(event.getDiary().getMember(), PointType.DIARY));
    }
}

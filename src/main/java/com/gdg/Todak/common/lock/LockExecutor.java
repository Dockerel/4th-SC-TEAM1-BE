package com.gdg.Todak.common.lock;

import com.gdg.Todak.common.lock.exception.LockException;
import com.gdg.Todak.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;

@RequiredArgsConstructor
@Component
public class LockExecutor {

    public static final int DELAY = 100;
    public static final int MULTIPLIER = 2;

    private final LockWithMemberFactory lockWithMemberFactory;

    @Retryable(
            value = {
                    LockException.class,
                    DeadlockLoserDataAccessException.class
            },
            backoff = @Backoff(delay = DELAY, multiplier = MULTIPLIER, random = true)
    )
    public void executeWithLock(String lockPrefix, Member member, Runnable runnable) {
        String lockKey = lockPrefix + member.getId();
        Lock lock = null;
        try {
            lock = lockWithMemberFactory.tryLock(member, lockKey, 10, 2);
            runnable.run();
        } finally {
            if (lock != null) {
                lockWithMemberFactory.unlock(member, lock);
            }
        }
    }
}

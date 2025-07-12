package com.gdg.Todak.common.lock;

import com.gdg.Todak.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;

@RequiredArgsConstructor
@Component
public class LockExecutor {

    private final LockWithMemberFactory lockWithMemberFactory;

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

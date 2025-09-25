package com.gdg.Todak.common.lock;

import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.service.PointLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static com.gdg.Todak.common.exception.errors.LockError.LOCK_ACQUIRE_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockWithMemberFactory implements LockWithMemberFactory {
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private final RedissonClient redissonClient;
    private final PointLogService pointLogService;

    @Override
    public Lock tryLock(Member member, String lockKey, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(waitTime, leaseTime, TIME_UNIT)) {
                log.error("획득 실패한 락: {}", lockKey);
                throw new TodakException(LOCK_ACQUIRE_ERROR);
            }
            return new RedisLock(lock);
        } catch (Exception e) {
            pointLogService.saveLockErrorLogToServer(member, "[Redis lock 획득 에러] Redis lock 획득 실패, errorMessage: " + e.getMessage());
            log.error("획득 실패한 락: {}", lockKey);
            throw new TodakException(LOCK_ACQUIRE_ERROR);
        }
    }

    @Override
    public void unlock(Member member, Lock lock) {
        try {
            if (lock instanceof RedisLock redisLock) {
                if (redisLock.isHeldByCurrentThread()) {
                    redisLock.unlock();
                } else {
                    pointLogService.saveLockErrorLogToServer(member, "[Redis lock 해제 에러] 현재 스레드는 락을 보유하고 있지 않습니다.");
                }
            } else {
                pointLogService.saveLockErrorLogToServer(member, "[Redis lock 해제 에러] 잘못된 락 객체 전달: " + lock.getClass().getName());
            }
        } catch (Exception e) {
            pointLogService.saveLockErrorLogToServer(member, "[Redis lock 해제 에러] 해제 실패: " + lock.getClass().getName());
        }
    }
}

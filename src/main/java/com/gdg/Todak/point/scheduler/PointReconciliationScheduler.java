package com.gdg.Todak.point.scheduler;

import com.gdg.Todak.common.lock.LockExecutor;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.service.MemberService;
import com.gdg.Todak.point.service.PointReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PointReconciliationScheduler {

    private final static String LOCK_PREFIX = "pointLock:";

    private final LockExecutor lockExecutor;
    private final PointReconciliationService pointReconciliationService;
    private final MemberService memberService;

    @Scheduled(cron = "0 0 4 * * *")
    public void reconcilePoints() {
        log.info("포인트 정합성 보정 작업 시작");

        LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        List<Long> memberIds = pointReconciliationService.getTargetMembers(start, end);
        memberIds.forEach(memberId -> reconcilePointWithLock(memberId));

        log.info("포인트 정합성 보정 작업 완료");
    }

    private void reconcilePointWithLock(Long memberId) {
        Member member = memberService.findMemberById(memberId);
        lockExecutor.executeWithLock(
                LOCK_PREFIX,
                member,
                () -> reconcilePoint(memberId)
        );
    }

    private void reconcilePoint(Long memberId) {
        try {
            pointReconciliationService.reconcilePoint(memberId);
        } catch (Exception e) {
            log.error("포인트 보정 중 오류 발생 | Member ID: {}", memberId, e);
        }
    }
}

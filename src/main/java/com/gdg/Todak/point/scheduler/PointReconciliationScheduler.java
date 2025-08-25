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

    private final PointReconciliationService pointReconciliationService;
    private final MemberService memberService;

    @Scheduled(cron = "0 0 4 * * *")
    public void reconcilePoints() {
        log.info("포인트 정합성 보정 작업 시작");

        LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime end = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay().minusNanos(1);

        List<Long> memberIds = pointReconciliationService.getTargetMembers(start, end);
        memberIds.forEach(memberId -> {
            Member member = memberService.findMemberById(memberId);
            pointReconciliationService.reconcilePointWithLock(member);
        });

        log.info("포인트 정합성 보정 작업 완료");
    }
}

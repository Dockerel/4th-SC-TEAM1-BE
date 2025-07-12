package com.gdg.Todak.point.scheduler;

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

    @Scheduled(cron = "0 0 4 * * *")
    public void reconcilePoints() {
        log.info("포인트 정합성 보정 작업 시작");

        LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        List<Long> memberIds = pointReconciliationService.getTargetMembers(start, end);
        for (Long memberId : memberIds) {
            try {
                pointReconciliationService.reconcilePoint(memberId);
            } catch (Exception e) {
                log.error("포인트 보정 중 오류 발생 | Member ID: {}", memberId, e);
            }
        }

        log.info("포인트 정합성 보정 작업 완료");
    }
}

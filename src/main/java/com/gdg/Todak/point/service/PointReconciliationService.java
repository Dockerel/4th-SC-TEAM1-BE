package com.gdg.Todak.point.service;

import com.gdg.Todak.common.lock.LockExecutor;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.entity.Point;
import com.gdg.Todak.point.repository.PointLogRepository;
import com.gdg.Todak.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PointReconciliationService {
    private final static String LOCK_PREFIX = "pointLock:";

    private final LockExecutor lockExecutor;
    private final PointLogRepository pointLogRepository;
    private final PointRepository pointRepository;

    @Transactional(readOnly = true)
    public List<Long> getTargetMembers(LocalDateTime start, LocalDateTime end) {
        ZoneId zone = ZoneId.systemDefault();

        LocalDate yesterday = LocalDate.now(zone).minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.atTime(LocalTime.MAX);

        Instant startInstant = startOfDay.atZone(zone).toInstant();
        Instant endInstant = endOfDay.atZone(zone).toInstant();
        return pointLogRepository.findMemberIdsWithActivityBetween(startInstant, endInstant);
    }

    @Async(value = "validationExecutor")
    @Transactional(readOnly = true)
    public void reconcilePointWithLock(Member member) {
        lockExecutor.executeWithLock(
                LOCK_PREFIX,
                member,
                () -> reconcilePoint(member)
        );
    }

    public void reconcilePoint(Member member) {
        Long memberId = member.getId();
        int calculatedTotalPoint = pointLogRepository.sumPointsByMemberId(memberId).orElse(0);

        Point findPoint = pointRepository.findByMemberId(memberId);
        int currentPoint = findPoint.getPoint();

        if (calculatedTotalPoint != currentPoint) {
            log.warn("포인트 불일치 | Member ID: {}, DB 저장값: {}, 로그 계산값: {}", memberId, currentPoint, calculatedTotalPoint);
            findPoint.updatePoint(calculatedTotalPoint);
        } else {
            log.info("포인트 일치 | Member ID: {}, DB 저장값: {}, 로그 계산값: {}", memberId, currentPoint, calculatedTotalPoint);
        }
    }
}

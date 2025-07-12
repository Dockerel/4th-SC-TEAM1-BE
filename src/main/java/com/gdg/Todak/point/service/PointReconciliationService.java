package com.gdg.Todak.point.service;

import com.gdg.Todak.point.entity.Point;
import com.gdg.Todak.point.repository.PointLogRepository;
import com.gdg.Todak.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PointReconciliationService {

    private final PointLogRepository pointLogRepository;
    private final PointRepository pointRepository;

    @Transactional(readOnly = true)
    public List<Long> getTargetMembers(LocalDateTime start, LocalDateTime end) {
        return pointLogRepository.findMemberIdsWithActivityBetween(start, end);
    }

    @Transactional
    public void reconcilePoint(Long memberId) {
        int calculatedTotalPoint = pointLogRepository.sumPointsByMemberId(memberId).orElse(0);

        Point findPoint = pointRepository.findByMemberId(memberId);
        int currentPoint = findPoint.getPoint();

        if (calculatedTotalPoint != currentPoint) {
            log.warn("포인트 불일치 | Member ID: {}, DB 저장값: {}, 로그 계산값: {}", memberId, currentPoint, calculatedTotalPoint);
            findPoint.updatePoint(calculatedTotalPoint);
        }
    }
}

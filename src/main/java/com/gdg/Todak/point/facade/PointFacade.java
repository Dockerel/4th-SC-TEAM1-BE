package com.gdg.Todak.point.facade;

import com.gdg.Todak.common.lock.LockExecutor;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.dto.PointRequest;
import com.gdg.Todak.point.service.PointService;
import com.gdg.Todak.tree.domain.GrowthButton;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointFacade {

    private final static String LOCK_PREFIX = "pointLock:";

    private final LockExecutor lockExecutor;
    private final PointService pointService;

    public void earnAttendancePointPerDay(Member member) {
        lockExecutor.executeWithLock(
                LOCK_PREFIX,
                member,
                () -> pointService.earnAttendancePointPerDay(member)
        );
    }

    public void earnPointByType(PointRequest pointRequest) {
        Member member = pointRequest.member();
        lockExecutor.executeWithLock(
                LOCK_PREFIX,
                member,
                () -> pointService.earnPointByType(pointRequest)
        );
    }

    public void consumePointByGrowthButton(Member member, GrowthButton growthButton) {
        lockExecutor.executeWithLock(
                LOCK_PREFIX,
                member,
                () -> pointService.consumePointByGrowthButton(member, growthButton)
        );
    }

    public void consumePointToGetCommentWriterId(Member member) {
        lockExecutor.executeWithLock(
                LOCK_PREFIX,
                member,
                () -> pointService.consumePointToGetCommentWriterId(member)
        );
    }
}

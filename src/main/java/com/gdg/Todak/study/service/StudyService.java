package com.gdg.Todak.study.service;

import com.gdg.Todak.common.lock.LockWithMemberFactory;
import com.gdg.Todak.diary.Emotion;
import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.diary.exception.BadRequestException;
import com.gdg.Todak.diary.repository.DiaryRepository;
import com.gdg.Todak.diary.service.SchedulerService;
import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.repository.FriendRepository;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.service.MemberService;
import com.gdg.Todak.notification.dto.PublishNotificationRequest;
import com.gdg.Todak.notification.service.NotificationService;
import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointLogRequest;
import com.gdg.Todak.point.dto.PointRequest;
import com.gdg.Todak.point.entity.Point;
import com.gdg.Todak.point.exception.NotFoundException;
import com.gdg.Todak.point.repository.PointLogRepository;
import com.gdg.Todak.point.repository.PointRepository;
import com.gdg.Todak.point.service.PointLogService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final static String LOCK_PREFIX = "pointLock:";

    private final static int ATTENDANCE_BASE_POINT = 10;
    private final static int ATTENDANCE_BONUS_2_DAYS = 15;
    private final static int ATTENDANCE_BONUS_3_DAYS = 20;
    private final static int ATTENDANCE_BONUS_4_DAYS = 25;
    private final static int ATTENDANCE_BONUS_5_DAYS = 30;
    private final static int DIARY_WRITE_POINT = 15;
    private final static int COMMENT_WRITE_POINT = 10;
    private final static int GET_COMMENT_WRITER_ID_POINT = 2;
    private final static List<PointType> ATTENDANCE_LISTS = Arrays.asList(
            PointType.ATTENDANCE_DAY_1,
            PointType.ATTENDANCE_DAY_2,
            PointType.ATTENDANCE_DAY_3,
            PointType.ATTENDANCE_DAY_4,
            PointType.ATTENDANCE_DAY_5_OR_MORE
    );

    private final MemberService memberService;
    private final DiaryRepository diaryRepository;
    private final NotificationService notificationService;
    private final FriendRepository friendRepository;
    private final SchedulerService schedulerService;
    private final PointRepository pointRepository;
    private final LockWithMemberFactory lockWithMemberFactory;
    private final PointLogRepository pointLogRepository;
    private final PointLogService pointLogService;


    @Transactional
    public Long saveDiary(String num) {
        Member member = memberService.findMemberByUserId("userId" + num);

        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.atTime(23, 59, 59, 99).atZone(ZoneId.systemDefault()).toInstant();

        if (diaryRepository.existsByMemberAndCreatedAtBetween(member, startOfDay, endOfDay)) {
            throw new BadRequestException("오늘 이미 작성된 일기 또는 감정이 있습니다. 삭제 후 재작성하거나 작성된 일기를 수정해주세요.");
        }

        Diary diary = Diary.builder()
                .member(member)
                .content("content")
                .emotion(Emotion.HAPPY)
                .storageUUID("storageUUID")
                .build();

        diary = diaryRepository.save(diary);

        String senderId = diary.getMember().getUserId();

        List<Friend> friends = friendRepository.findAllByAccepterUserIdAndFriendStatusOrRequesterUserIdAndFriendStatus(
                senderId, FriendStatus.ACCEPTED, // requester
                senderId, FriendStatus.ACCEPTED // acceptor
        );

        for (Friend friend : friends) {
            String receiverId = friend.getFriend(senderId).getUserId();
            PublishNotificationRequest request = PublishNotificationRequest.of(senderId, receiverId, "diary", diary.getId(), diary.getCreatedAt());
            notificationService.publishNotification(request);
        }

        schedulerService.scheduleSavingCommentByAI(diary);


        PointRequest pointRequest = PointRequest.of(diary.getMember(), PointType.DIARY);
        earnPointByType(pointRequest);

        return diary.getId();
    }

    public void earnPointByType(PointRequest pointRequest) {
        String lockKey = "LOCK_PREFIX" + pointRequest.member().getId();

        Lock lock = lockWithMemberFactory.tryLock(pointRequest.member(), lockKey, 10, 2);

        Point point = getPoint(pointRequest.member());

        Instant startOfDay = Instant.now().atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS).minusMillis(1);

        int pointByType = getPointByType(pointRequest.pointType());

        if (!pointLogRepository.existsByCreatedAtBetweenAndMemberAndPointTypeIn(startOfDay, endOfDay, pointRequest.member(), List.of(pointRequest.pointType()))) {
            point.earnPoint(pointByType);
            pointLogService.createPointLog(new PointLogRequest(pointRequest.member(), pointByType, pointRequest.pointType(), PointStatus.EARNED, LocalDateTime.now()));
        }

        lockWithMemberFactory.unlock(pointRequest.member(), lock);
    }

    private Point getPoint(Member member) {
        return pointRepository.findByMember(member)
                .orElseThrow(() -> new NotFoundException("member의 point 객체가 없습니다."));
    }

    private int getPointByType(PointType pointType) {
        return switch (pointType) {
            case DIARY -> DIARY_WRITE_POINT;
            case COMMENT -> COMMENT_WRITE_POINT;
            default -> throw new BadRequestException("해당하는 pointType이 없습니다");
        };
    }

    @Transactional
    public void transactionalTest() {
        System.out.println("StudyService.transactionalTest");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

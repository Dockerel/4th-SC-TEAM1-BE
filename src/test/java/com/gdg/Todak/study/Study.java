package com.gdg.Todak.study;

import com.gdg.Todak.diary.service.DiaryService;
import com.gdg.Todak.friend.dto.FriendIdRequest;
import com.gdg.Todak.friend.dto.FriendResponse;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.service.FriendService;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.service.MemberService;
import com.gdg.Todak.member.service.request.SignupServiceRequest;
import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointLogRequest;
import com.gdg.Todak.point.entity.Point;
import com.gdg.Todak.point.repository.PointRepository;
import com.gdg.Todak.point.service.PointLogService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Transactional
@SpringBootTest
public class Study {

    public static final int MEMBER_CNT = 100;

    public static final int MAX_MEMBER_CNT = 20;

    @Autowired
    MemberService memberService;
    @Autowired
    FriendService friendService;
    @Autowired
    DiaryService diaryService;
    @Autowired
    PointLogService pointLogService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PointRepository pointRepository;
    @Autowired
    EntityManager em;

    @Commit
    @Test
    @Order(1)
    void createMembers() {
        for (int i = 200; i < 1000; i++) {
            String num = String.valueOf(i);
            SignupServiceRequest request = SignupServiceRequest.of("userId" + num, "password", "password", "user" + num);
            memberService.signup(request);
            if (i % 200 == 0) {
                em.flush();
                em.clear();
            }
        }
    }

    @Commit
//    @Test
    @Order(2)
    void connectAllEachMembersAsFriends() {
        for (int k = 0; k < MEMBER_CNT / MAX_MEMBER_CNT; k++) {
            int start = MAX_MEMBER_CNT * k;
            int end = MAX_MEMBER_CNT * k + MAX_MEMBER_CNT <= MEMBER_CNT ? MAX_MEMBER_CNT * k + MAX_MEMBER_CNT : MEMBER_CNT;
            for (int i = start; i < end - 1; i++) {
                for (int j = i + 1; j < end; j++) {
                    String requestor = String.valueOf(i);
                    String acceptor = String.valueOf(j);
                    String userId = "userId" + requestor;
                    String friendId = "userId" + acceptor;
                    FriendIdRequest makeFriendRequest = new FriendIdRequest(friendId);
                    Friend friendRequest = friendService.makeFriendRequest(userId, makeFriendRequest);
                    friendService.acceptFriendRequest(friendId, friendRequest.getId());
                }
            }
        }
    }

    //    @Test
    @Order(3)
    void findFriends() {
        List<FriendResponse> friends = friendService.getAllFriend("userId76");
        for (FriendResponse friend : friends) {
            System.out.println("friend = " + friend);
        }
    }

    @Commit
    @Test
    void createPointLogs() {
        List<Member> members = memberRepository.findAll();
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        PointLogRequest request;

        int start = 400;
        int end = 1000;

        for (int k = start; k < end; k++) {
            log.info("{} start", k);
            Member member = members.get(k);
            Point point = pointRepository.findByMember(member).get();

            request = new PointLogRequest(member, 200000, PointType.COMMENT, PointStatus.EARNED, createdAt);
            point.earnPoint(200000);
            pointLogService.createPointLog(request);

            for (int i = 0; i < 10000; i++) {
                int randomNumber = random.nextInt(2);
                int randomPoint = random.nextInt(1, 100);
                if (randomNumber % 2 == 0) {
                    request = new PointLogRequest(member, randomPoint, PointType.COMMENT, PointStatus.EARNED, createdAt);
                    point.earnPoint(randomPoint);
                } else {
                    request = new PointLogRequest(member, randomPoint, PointType.GROWTH_WATER, PointStatus.CONSUMED, createdAt);
                    point.consumePoint(randomPoint);
                }
                pointLogService.createPointLog(request);

                if (i % 500 == 0) {
                    em.flush();
                    em.clear();
                }
            }
        }
    }

    @Test
    @Commit
    void test() {
        List<Point> points = pointRepository.findAll();
        for (Point point : points) {
            point.consumePoint(1);
        }
    }
}
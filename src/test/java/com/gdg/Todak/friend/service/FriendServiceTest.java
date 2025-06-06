package com.gdg.Todak.friend.service;

import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.dto.*;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.exception.BadRequestException;
import com.gdg.Todak.friend.repository.FriendRepository;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class FriendServiceTest {

    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member requester;
    private Member accepter;

    @BeforeEach
    void setUp() {
        requester = memberRepository.save(new Member("requesterUser", "test1", "test1", "test1", "test1"));
        accepter = memberRepository.save(new Member("accepterUser", "test2", "test2", "test2", "test2"));
    }

    @Test
    @DisplayName("친구 요청 성공")
    void makeFriendRequestSuccessfullyTest() {
        //given
        FriendIdRequest request = new FriendIdRequest(accepter.getUserId());

        //when
        friendService.makeFriendRequest(requester.getUserId(), request);

        //then
        Optional<Friend> friendRequest = friendRepository.findByRequesterAndAccepter(requester, accepter);
        assertThat(friendRequest).isPresent();
        assertThat(friendRequest.get().getFriendStatus()).isEqualTo(FriendStatus.PENDING);
    }

    @Test
    @DisplayName("본인에게 친구 요청 불가")
    void notAllowSelfFriendRequestTest() {
        //given
        FriendIdRequest request = new FriendIdRequest(requester.getUserId());

        //when & then
        assertThatThrownBy(() -> friendService.makeFriendRequest(requester.getUserId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("본인에게는 친구 요청을 할 수 없습니다");
    }

    @Test
    @DisplayName("중복 친구 요청 불가")
    void notAllowDuplicateFriendRequestTest() {
        //given
        FriendIdRequest request = new FriendIdRequest(accepter.getUserId());

        //when
        friendService.makeFriendRequest(requester.getUserId(), request);

        //then
        assertThatThrownBy(() -> friendService.makeFriendRequest(requester.getUserId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 친구이거나, 대기 또는 거절된 친구요청이 존재합니다.");
    }

    @Test
    @DisplayName("친구 요청 수락")
    void acceptFriendRequestTest() {
        //given
        Friend friend = friendRepository.save(Friend.builder().requester(requester).accepter(accepter).friendStatus(FriendStatus.PENDING).build());

        //when
        friendService.acceptFriendRequest(accepter.getUserId(), friend.getId());

        //then
        Friend updatedFriend = friendRepository.findById(friend.getId()).orElseThrow();
        assertThat(updatedFriend.getFriendStatus()).isEqualTo(FriendStatus.ACCEPTED);
    }

    @Test
    @DisplayName("친구 요청 거절")
    void declineFriendRequestTest() {
        //given
        Friend friend = friendRepository.save(Friend.builder().requester(requester).accepter(accepter).friendStatus(FriendStatus.PENDING).build());

        //when
        friendService.declineFriendRequest(accepter.getUserId(), friend.getId());

        //then
        Friend updatedFriend = friendRepository.findById(friend.getId()).orElseThrow();
        assertThat(updatedFriend.getFriendStatus()).isEqualTo(FriendStatus.DECLINED);
    }

    @Test
    @DisplayName("친구 삭제")
    void deleteFriendTest() {
        //given
        Friend friend = friendRepository.save(Friend.builder().requester(requester).accepter(accepter).friendStatus(FriendStatus.ACCEPTED).build());

        //when
        friendService.deleteFriend(requester.getUserId(), friend.getId());

        //then
        Optional<Friend> deletedFriend = friendRepository.findById(friend.getId());
        assertThat(deletedFriend).isEmpty();
    }

    @Test
    @DisplayName("친구 요청 개수를 초과한 경우 예외 발생")
    void exceedFriendRequestLimitTest() {
        // given
        FriendIdRequest request = new FriendIdRequest(accepter.getUserId());

        for (int i = 0; i < 20; i++) {
            Member accepterMember = Member.builder()
                    .userId("accepter" + i)
                    .password("password")
                    .salt("salt")
                    .imageUrl("imageUrl")
                    .build();

            memberRepository.save(accepterMember);

            Friend friendRequest = Friend.builder()
                    .requester(requester)
                    .accepter(accepterMember)
                    .friendStatus(FriendStatus.PENDING)
                    .build();

            friendRepository.save(friendRequest);
        }

        // when & then
        assertThatThrownBy(() -> friendService.makeFriendRequest(requester.getUserId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("친구 요청 개수를 초과하였습니다. (최대 20개)");
    }

    @Test
    @DisplayName("상대방이 더 이상 친구 요청을 받을 수 없을 경우 예외 발생")
    void exceedAccepterFriendRequestLimitTest() {
        // given
        FriendIdRequest request = new FriendIdRequest(accepter.getUserId());

        for (int i = 0; i < 20; i++) {
            Member requesterMember = Member.builder()
                    .userId("requester" + i)
                    .password("password")
                    .salt("salt")
                    .imageUrl("imageUrl")
                    .build();

            memberRepository.save(requesterMember);

            Friend friendRequest = Friend.builder()
                    .requester(requesterMember)
                    .accepter(accepter)
                    .friendStatus(FriendStatus.PENDING)
                    .build();

            friendRepository.save(friendRequest);
        }

        // when & then
        assertThatThrownBy(() -> friendService.makeFriendRequest(requester.getUserId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("상대방이 더 이상 친구 요청을 받을 수 없습니다. (최대 20개)");
    }

    @Test
    @DisplayName("모든 친구 조회")
    void getAllFriendTest() {
        //given
        Friend friend = friendRepository.save(Friend.builder().requester(requester).accepter(accepter).friendStatus(FriendStatus.ACCEPTED).build());

        //when
        List<FriendResponse> friendResponses = friendService.getAllFriend(requester.getUserId());

        //then
        assertThat(friendResponses).hasSize(1);
        assertThat(friendResponses.get(0).friendId()).isEqualTo(accepter.getUserId());
    }

    @Test
    @DisplayName("본인이 보낸 대기 및 거절된 친구 요청 조회")
    void getAllPendingAndDeclinedFriendRequestByRequesterTest() {
        //given
        Friend pendingFriend = friendRepository.save(Friend.builder()
                .requester(requester)
                .accepter(accepter)
                .friendStatus(FriendStatus.PENDING)
                .build());

        Member anotherAccepter = memberRepository.save(new Member("another", "test3", "test3", "test3", "test3"));
        Friend declinedFriend = friendRepository.save(Friend.builder()
                .requester(requester)
                .accepter(anotherAccepter)
                .friendStatus(FriendStatus.DECLINED)
                .build());

        //when
        List<FriendRequestWithStatusResponse> friendRequestResponses =
                friendService.getAllPendingAndDeclinedFriendRequestByRequester(requester.getUserId());

        //then
        assertThat(friendRequestResponses).hasSize(2);
        assertThat(friendRequestResponses.stream().map(FriendRequestWithStatusResponse::friendStatus))
                .containsExactlyInAnyOrder(FriendStatus.PENDING, FriendStatus.DECLINED);
    }

    @Test
    @DisplayName("본인이 받은 대기 및 거절된 친구 요청 조회")
    void getAllPendingAndDeclinedFriendRequestByAccepterTest() {
        //given
        Friend pendingFriend = friendRepository.save(Friend.builder()
                .requester(accepter)
                .accepter(requester)
                .friendStatus(FriendStatus.PENDING)
                .build());

        Member anotherRequester = memberRepository.save(new Member("another", "test3", "test3", "test3", "test3"));
        Friend declinedFriend = friendRepository.save(Friend.builder()
                .requester(anotherRequester)
                .accepter(requester)
                .friendStatus(FriendStatus.DECLINED)
                .build());

        //when
        List<FriendRequestWithStatusResponse> friendRequestResponses =
                friendService.getAllPendingAndDeclinedFriendRequestByAccepter(requester.getUserId());

        //then
        assertThat(friendRequestResponses).hasSize(2);
        assertThat(friendRequestResponses.stream().map(FriendRequestWithStatusResponse::friendStatus))
                .containsExactlyInAnyOrder(FriendStatus.PENDING, FriendStatus.DECLINED);
    }

    @Test
    @DisplayName("모든 대기중인 친구 요청 조회")
    void getAllFriendRequestsTest() {
        //given
        Friend pendingRequest = friendRepository.save(Friend.builder()
                .requester(accepter)
                .accepter(requester)
                .friendStatus(FriendStatus.PENDING)
                .build());

        //when
        List<FriendRequestResponse> friendRequestResponses = friendService.getAllFriendRequests(requester.getUserId());

        //then
        assertThat(friendRequestResponses).hasSize(1);
        assertThat(friendRequestResponses.get(0).requesterName()).isEqualTo(accepter.getUserId());
        assertThat(friendRequestResponses.get(0).accepterName()).isEqualTo(requester.getUserId());
    }

    @Test
    @DisplayName("사용자의 친구 및 친구 요청 개수 조회")
    void getMyFriendCountByStatusTest() {
        //given
        // 대기 중인 요청
        Friend pendingFriend1 = friendRepository.save(Friend.builder()
                .requester(requester)
                .accepter(accepter)
                .friendStatus(FriendStatus.PENDING)
                .build());

        // 수락된 요청
        Member anotherAccepter = memberRepository.save(new Member("another", "test3", "test3", "test3", "test3"));
        Friend acceptedFriend = friendRepository.save(Friend.builder()
                .requester(requester)
                .accepter(anotherAccepter)
                .friendStatus(FriendStatus.ACCEPTED)
                .build());

        //when
        List<FriendCountResponse> friendCountResponses = friendService.getMyFriendCountByStatus(requester.getUserId());

        //then
        assertThat(friendCountResponses).hasSize(3);
        assertThat(friendCountResponses.stream()
                .filter(resp -> resp.friendStatus() == FriendStatus.PENDING)
                .findFirst()
                .orElseThrow()
                .count()).isEqualTo(1);
        assertThat(friendCountResponses.stream()
                .filter(resp -> resp.friendStatus() == FriendStatus.ACCEPTED)
                .findFirst()
                .orElseThrow()
                .count()).isEqualTo(1);
    }
}

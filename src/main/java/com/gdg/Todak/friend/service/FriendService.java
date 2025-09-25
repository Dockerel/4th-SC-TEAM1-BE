package com.gdg.Todak.friend.service;

import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.friend.dto.*;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.entity.FriendStatus;
import com.gdg.Todak.friend.repository.FriendRepository;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.gdg.Todak.common.exception.errors.FriendError.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Friend makeFriendRequest(String userId, FriendIdRequest friendIdRequest) {
        Member requesterMember = getMember(userId);
        Member accepterMember = memberRepository.findByUserId(friendIdRequest.friendId())
                .orElseThrow(() -> new TodakException(MEMBER_NOT_FOUND_BY_FRIEND_ID_ERROR));

        if (friendRepository.existsByRequesterAndAccepter(requesterMember, accepterMember) || friendRepository.existsByRequesterAndAccepter(accepterMember, requesterMember)) {
            throw new TodakException(ALREADY_PROCESSED_REQUEST_ERROR);
        }

        if (requesterMember.equals(accepterMember)) {
            throw new TodakException(SELF_REQUEST_ERROR);
        }

        long requesterFriendCount = friendRepository.countByRequesterAndStatusIn(requesterMember, List.of(FriendStatus.PENDING, FriendStatus.ACCEPTED));
        if (requesterFriendCount >= 20) {
            throw new TodakException(MY_FRIEND_REQUEST_LIMIT_ERROR);
        }

        long accepterFriendCount = friendRepository.countByAccepterAndStatusIn(accepterMember, List.of(FriendStatus.PENDING, FriendStatus.ACCEPTED));
        if (accepterFriendCount >= 20) {
            throw new TodakException(OPPOSITE_FRIEND_REQUEST_LIMIT_ERROR);
        }

        Friend friend = friendRepository.save(Friend.builder()
                .requester(requesterMember)
                .accepter(accepterMember)
                .friendStatus(FriendStatus.PENDING)
                .build());

        return friend;
    }

    public List<FriendResponse> getAllFriend(String userId) {
        return friendRepository.findAllByAccepterUserIdAndFriendStatusOrRequesterUserIdAndFriendStatus(userId, FriendStatus.ACCEPTED, userId, FriendStatus.ACCEPTED)
                .stream().map(friend -> {
                    if (friend.getRequester().getUserId().equals(userId)) {
                        return new FriendResponse(
                                friend.getId(),
                                friend.getAccepter().getUserId()
                        );
                    } else {
                        return new FriendResponse(
                                friend.getId(),
                                friend.getRequester().getUserId()
                        );
                    }
                })
                .collect(Collectors.toList());
    }

    public List<FriendRequestWithStatusResponse> getAllPendingAndDeclinedFriendRequestByRequester(String userId) {
        return friendRepository.findAllByRequesterUserIdAndFriendStatusIn(userId, List.of(FriendStatus.PENDING, FriendStatus.DECLINED))
                .stream().map(
                        Friend -> new FriendRequestWithStatusResponse(
                                Friend.getId(),
                                Friend.getRequester().getUserId(),
                                Friend.getAccepter().getUserId(),
                                Friend.getFriendStatus()
                        ))
                .toList();
    }

    public List<FriendRequestWithStatusResponse> getAllPendingAndDeclinedFriendRequestByAccepter(String userId) {
        return friendRepository.findAllByAccepterUserIdAndFriendStatusIn(userId, List.of(FriendStatus.PENDING, FriendStatus.DECLINED))
                .stream().map(
                        Friend -> new FriendRequestWithStatusResponse(
                                Friend.getId(),
                                Friend.getRequester().getUserId(),
                                Friend.getAccepter().getUserId(),
                                Friend.getFriendStatus()
                        ))
                .toList();
    }

    public List<FriendRequestResponse> getAllFriendRequests(String userId) {
        return friendRepository.findAllByAccepterUserIdAndFriendStatusOrRequesterUserIdAndFriendStatus(userId, FriendStatus.PENDING, userId, FriendStatus.PENDING)
                .stream().map(
                        Friend -> new FriendRequestResponse(
                                Friend.getId(),
                                Friend.getRequester().getUserId(),
                                Friend.getAccepter().getUserId()
                        ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptFriendRequest(String userId, Long friendRequestId) {
        Member member = getMember(userId);

        Friend friendRequest = friendRepository.findById(friendRequestId)
                .orElseThrow(() -> new TodakException(REQUEST_NOT_FOUND_BY_FRIEND_REQUEST_ID_ERROR));

        if (friendRequest.checkMemberIsNotAccepter(member)) {
            throw new TodakException(NOT_ALLOWED_TO_ACCEPT_ERROR);
        }

        friendRequest.acceptFriendRequest();
    }

    @Transactional
    public void declineFriendRequest(String userId, Long friendRequestId) {
        Member member = getMember(userId);

        Friend friendRequest = friendRepository.findById(friendRequestId)
                .orElseThrow(() -> new TodakException(REQUEST_NOT_FOUND_BY_FRIEND_REQUEST_ID_ERROR));

        if (friendRequest.checkMemberIsNotAccepter(member)) {
            throw new TodakException(NOT_ALLOWED_TO_DECLINE_ERROR);
        }

        friendRequest.declinedFriendRequest();
    }

    @Transactional
    public void deleteFriend(String userId, Long friendRequestId) {
        Member member = getMember(userId);

        Friend friendRequest = friendRepository.findById(friendRequestId)
                .orElseThrow(() -> new TodakException(REQUEST_NOT_FOUND_BY_FRIEND_REQUEST_ID_ERROR));

        if (friendRequest.checkMemberIsNotRequester(member) && friendRequest.checkMemberIsNotAccepter(member)) {
            throw new TodakException(NOT_ALLOWED_TO_DELETE_FRIEND_ERROR);
        }

        friendRepository.deleteById(friendRequestId);
    }

    public List<FriendCountResponse> getMyFriendCountByStatus(String userId) {
        Member member = getMember(userId);

        long PendingFriendCountByAccepter = friendRepository.countByRequesterAndStatusIn(member, List.of(FriendStatus.PENDING));
        long PendingFriendCountByRequester = friendRepository.countByAccepterAndStatusIn(member, List.of(FriendStatus.PENDING));
        long AcceptedFriendCount = friendRepository.countByRequesterAndStatusIn(member, List.of(FriendStatus.ACCEPTED))
                + friendRepository.countByAccepterAndStatusIn(member, List.of(FriendStatus.ACCEPTED));

        return List.of(
                new FriendCountResponse(FriendStatus.PENDING, "Accepter", PendingFriendCountByAccepter),
                new FriendCountResponse(FriendStatus.PENDING, "Requester", PendingFriendCountByRequester),
                new FriendCountResponse(FriendStatus.ACCEPTED, "Accepter or Requester", AcceptedFriendCount)
        );
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new TodakException(MEMBER_NOT_FOUND_BY_USER_ID_ERROR));
    }
}

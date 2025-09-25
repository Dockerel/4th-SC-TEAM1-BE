package com.gdg.Todak.friend.service;

import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.entity.FriendStatus;
import com.gdg.Todak.friend.repository.FriendRepository;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.gdg.Todak.common.exception.errors.FriendError.MEMBER_NOT_FOUND_BY_USER_ID_ERROR;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FriendCheckService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;

    public List<Member> getFriendMembers(String userId) {
        Member member = getMember(userId);

        List<Friend> acceptedFriends = friendRepository.findAllByAccepterUserIdAndFriendStatusOrRequesterUserIdAndFriendStatus(
                userId, FriendStatus.ACCEPTED, userId, FriendStatus.ACCEPTED);

        return acceptedFriends.stream()
                .map(friend -> friend.getAccepter().equals(member) ? friend.getRequester() : friend.getAccepter())
                .toList();
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new TodakException(MEMBER_NOT_FOUND_BY_USER_ID_ERROR));
    }
}

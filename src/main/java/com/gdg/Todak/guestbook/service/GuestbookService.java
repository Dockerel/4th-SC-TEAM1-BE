package com.gdg.Todak.guestbook.service;

import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.friend.service.FriendCheckService;
import com.gdg.Todak.guestbook.controller.dto.AddGuestbookRequest;
import com.gdg.Todak.guestbook.controller.dto.AddGuestbookResponse;
import com.gdg.Todak.guestbook.controller.dto.DeleteGuestbookRequest;
import com.gdg.Todak.guestbook.controller.dto.GetGuestbookResponse;
import com.gdg.Todak.guestbook.entity.Guestbook;
import com.gdg.Todak.guestbook.repository.GuestbookRepository;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.gdg.Todak.common.exception.errors.GuestbookError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class GuestbookService {

    public static final int EXPIRE_DAY = 1;

    private final GuestbookRepository guestbookRepository;
    private final MemberRepository memberRepository;
    private final FriendCheckService friendCheckService;

    public List<GetGuestbookResponse> getMyGuestbook(AuthenticateUser user) {
        Member member = getMember(user.getUserId());

        return guestbookRepository.findValidGuestbooksByReceiverUserId(member.getUserId()).stream()
                .map(GetGuestbookResponse::from)
                .toList();
    }

    public List<GetGuestbookResponse> getFriendGuestbook(AuthenticateUser user, String friendId) {
        Member member = getMember(user.getUserId());

        List<Member> acceptedMembers = friendCheckService.getFriendMembers(friendId);

        if (!acceptedMembers.contains(member)) {
            throw new TodakException(NOT_ALLOWED_TO_LOOK_UP_GUESTBOOK_ERROR);
        }

        return guestbookRepository.findValidGuestbooksByReceiverUserId(friendId).stream()
                .map(GetGuestbookResponse::from)
                .toList();
    }

    @Transactional
    public AddGuestbookResponse addGuestbook(AuthenticateUser user, AddGuestbookRequest request) {
        Member sender = getMember(user.getUserId());
        Member receiver = getMember(request.getUserId());

        Instant expiresAt = Instant.now().plus(EXPIRE_DAY, ChronoUnit.DAYS);

        List<Member> acceptedMembers = friendCheckService.getFriendMembers(receiver.getUserId());

        if (!acceptedMembers.contains(sender)) {
            throw new TodakException(NOT_ALLOWED_TO_WRITE_GUESTBOOK_ERROR);
        }

        Guestbook guestbook = Guestbook.of(sender, receiver, request.getContent(), expiresAt);

        return AddGuestbookResponse.from(guestbookRepository.save(guestbook));
    }

    @Transactional
    public String deleteGuestbook(AuthenticateUser user, DeleteGuestbookRequest request) {
        Member member = getMember(user.getUserId());

        Guestbook guestbook = getMyGuestbook(request);

        if (isNotGuestbookOwner(member, guestbook)) {
            throw new TodakException(NOT_GUESTBOOK_OWNER_ERROR);
        }

        guestbookRepository.delete(guestbook);

        return "방명록이 삭제되었습니다.";
    }

    @Transactional
    public void deleteExpiredGuestbooks(Instant now) {
        guestbookRepository.deleteAllExpiredGuestbooks(now);
    }

    private static boolean isNotGuestbookOwner(Member sender, Guestbook guestbook) {
        return !sender.getUserId().equals(guestbook.getReceiver().getUserId());
    }

    private Guestbook getMyGuestbook(DeleteGuestbookRequest request) {
        Optional<Guestbook> guestbookOptional = guestbookRepository.findById(request.getGuestbookId());

        if (guestbookOptional.isEmpty()) {
            throw new TodakException(GUESTBOOK_NOT_FOUND_ERROR);
        }

        return guestbookOptional.get();
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new TodakException(MEMBER_NOT_FOUND_ERROR));
    }
}

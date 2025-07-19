package com.gdg.Todak.friend.facade;

import com.gdg.Todak.event.event.NewFriendRequestEvent;
import com.gdg.Todak.friend.dto.FriendIdRequest;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FriendFacade {

    private final FriendService friendService;
    private final ApplicationEventPublisher eventPublisher;

    public void makeFriendRequest(String userId, FriendIdRequest friendIdRequest) {
        Friend friend = friendService.makeFriendRequest(userId, friendIdRequest);
        eventPublisher.publishEvent(NewFriendRequestEvent.of(friend));
    }
}

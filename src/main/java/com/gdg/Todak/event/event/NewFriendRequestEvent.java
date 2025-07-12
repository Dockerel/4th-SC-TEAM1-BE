package com.gdg.Todak.event.event;

import com.gdg.Todak.friend.entity.Friend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NewFriendRequestEvent {
    private Friend friend;

    public static NewFriendRequestEvent of(Friend friend) {
        return NewFriendRequestEvent.builder()
                .friend(friend)
                .build();
    }
}

package com.gdg.Todak.friend.dto;

import com.gdg.Todak.friend.entity.FriendStatus;

public record FriendCountResponse(
        FriendStatus friendStatus,
        String info,
        long count
) {
}

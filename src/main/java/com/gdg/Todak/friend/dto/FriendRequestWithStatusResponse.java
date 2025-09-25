package com.gdg.Todak.friend.dto;

import com.gdg.Todak.friend.entity.FriendStatus;

public record FriendRequestWithStatusResponse(
        Long friendRequestId,
        String requesterName,
        String accepterName,
        FriendStatus friendStatus
) {
}

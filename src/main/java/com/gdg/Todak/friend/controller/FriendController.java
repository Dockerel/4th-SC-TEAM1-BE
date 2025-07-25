package com.gdg.Todak.friend.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.friend.dto.*;
import com.gdg.Todak.friend.facade.FriendFacade;
import com.gdg.Todak.friend.service.FriendService;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.resolver.Login;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friends")
@Tag(name = "친구", description = "친구 관련 API")
public class FriendController {

    private final FriendFacade friendFacade;
    private final FriendService friendService;

    @Operation(summary = "친구 요청 보내기", description = "친구의 이름을 기반으로 친구요청합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> sendFriendRequest(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser, @RequestBody FriendIdRequest friendIdRequest) {
        friendFacade.makeFriendRequest(authenticateUser.getUserId(), friendIdRequest);
        return ApiResponse.of(HttpStatus.CREATED, "친구 요청이 생성되었습니다.");
    }

    @Operation(summary = "친구 확인", description = "본인의 친구들을 확인합니다.")
    @GetMapping
    public ApiResponse<List<FriendResponse>> getAllFriends(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser) {
        List<FriendResponse> friendResponses = friendService.getAllFriend(authenticateUser.getUserId());
        return ApiResponse.ok(friendResponses);
    }

    @Operation(summary = "대기중인 친구 요청들 확인", description = "친구 요청 대기 목록을 확인합니다.")
    @GetMapping("/pending")
    public ApiResponse<List<FriendRequestResponse>> getAllPendingFriendRequest(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser) {
        List<FriendRequestResponse> friendRequestResponses = friendService.getAllFriendRequests(authenticateUser.getUserId());
        return ApiResponse.ok(friendRequestResponses);
    }

    @Operation(summary = "보낸 친구 요청들 확인", description = "본인이 보낸 친구 요청들 중 대기중, 거절된 친구 요청들을 확인합니다.")
    @GetMapping("/requester")
    public ApiResponse<List<FriendRequestWithStatusResponse>> getAllPendingAndDeclinedFriendRequestByRequester(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser) {
        List<FriendRequestWithStatusResponse> friendRequestResponses = friendService.getAllPendingAndDeclinedFriendRequestByRequester(authenticateUser.getUserId());
        return ApiResponse.ok(friendRequestResponses);
    }

    @Operation(summary = "받은 친구 요청들 확인", description = "본인이 받은 친구 요청들 중 대기중, 거절된 친구 요청들을 확인합니다.")
    @GetMapping("/accepter")
    public ApiResponse<List<FriendRequestWithStatusResponse>> getAllPendingAndDeclinedFriendRequestByAccepter(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser) {
        List<FriendRequestWithStatusResponse> friendRequestResponses = friendService.getAllPendingAndDeclinedFriendRequestByAccepter(authenticateUser.getUserId());
        return ApiResponse.ok(friendRequestResponses);
    }

    @Operation(summary = "친구 요청 수락", description = "친구 요청을 수락합니다.")
    @PutMapping("/accept/{friendRequestId}")
    public ApiResponse<Void> acceptFriendRequest(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser, @PathVariable Long friendRequestId) {
        friendService.acceptFriendRequest(authenticateUser.getUserId(), friendRequestId);
        return ApiResponse.of(HttpStatus.OK, "친구 요청이 수락되었습니다.");
    }

    @Operation(summary = "친구 요청 거절", description = "친구 요청을 거절합니다.")
    @PutMapping("/decline/{friendRequestId}")
    public ApiResponse<Void> declineFriendRequest(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser, @PathVariable Long friendRequestId) {
        friendService.declineFriendRequest(authenticateUser.getUserId(), friendRequestId);
        return ApiResponse.of(HttpStatus.OK, "친구 요청을 거절하였습니다.");
    }

    @Operation(summary = "친구 또는 친구요청 삭제", description = "친구를 삭제합니다.")
    @DeleteMapping("/{friendRequestId}")
    public ApiResponse<Void> deleteFriend(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser, @PathVariable Long friendRequestId) {
        friendService.deleteFriend(authenticateUser.getUserId(), friendRequestId);
        return ApiResponse.of(HttpStatus.OK, "친구/친구요청을 삭제하였습니다.");
    }

    @Operation(summary = "대기중인 친구요청 및 수락된 친구요청의 수 확인", description = "본인이 요청한 친구요청 중 대기중, 본인이 받은 친구요청 중 대기중, 본인이 보내거나 받은 친구요청 중 수락된 친구요청의 수를 각각 표시합니다.")
    @GetMapping("/count")
    public ApiResponse<List<FriendCountResponse>> getMyFriendCount(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser) {
        List<FriendCountResponse> friendCountResponses = friendService.getMyFriendCountByStatus(authenticateUser.getUserId());
        return ApiResponse.ok(friendCountResponses);
    }
}

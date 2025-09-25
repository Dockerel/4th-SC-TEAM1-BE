package com.gdg.Todak.common.exception.errors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
public enum FriendError implements ErrorInfo {

    NOT_PENDING_REQUEST_ERROR(BAD_REQUEST, "본인이 작성한 댓글은 익명 해제가 필요하지 않습니다."),
    ALREADY_PROCESSED_REQUEST_ERROR(BAD_REQUEST, "이미 친구이거나, 대기 또는 거절된 친구요청이 존재합니다."),
    SELF_REQUEST_ERROR(BAD_REQUEST, "본인에게는 친구 요청을 할 수 없습니다"),
    MY_FRIEND_REQUEST_LIMIT_ERROR(BAD_REQUEST, "친구 요청 개수를 초과하였습니다. (최대 20개)"),
    OPPOSITE_FRIEND_REQUEST_LIMIT_ERROR(BAD_REQUEST, "상대방이 더 이상 친구 요청을 받을 수 없습니다. (최대 20개)"),

    MEMBER_NOT_FOUND_BY_USER_ID_ERROR(NOT_FOUND, "userId에 해당하는 멤버가 없습니다."),
    MEMBER_NOT_FOUND_BY_FRIEND_ID_ERROR(NOT_FOUND, "diary id에 해당하는 일기가 없습니다."),
    REQUEST_NOT_FOUND_BY_FRIEND_REQUEST_ID_ERROR(NOT_FOUND, "userId에 해당하는 멤버가 없습니다."),

    NOT_ALLOWED_TO_ACCEPT_ERROR(UNAUTHORIZED, "친구 요청을 수락할 권한이 없습니다. 요청받은 사람만 수락할 수 있습니다."),
    NOT_ALLOWED_TO_DECLINE_ERROR(UNAUTHORIZED, "친구 요청을 거절할 권한이 없습니다. 요청받은 사람만 거절할 수 있습니다."),
    NOT_ALLOWED_TO_DELETE_FRIEND_ERROR(UNAUTHORIZED, "친구를 삭제할 권한이 없습니다. 당사자들만 삭제할 수 있습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus status() {
        return this.status;
    }

    @Override
    public String message() {
        return this.message;
    }
}

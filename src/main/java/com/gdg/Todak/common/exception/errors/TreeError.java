package com.gdg.Todak.common.exception.errors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
public enum TreeError implements ErrorInfo {

    TREE_LIMIT_ERROR(BAD_REQUEST, "한 멤버당 소유할 수 있는 나무의 수는 한그루 입니다."),
    MAX_LEVEL_ERROR(BAD_REQUEST, "최고 레벨입니다."),

    MEMBER_TREE_NOT_FOUND_ERROR(NOT_FOUND, "member의 tree가 없습니다."),

    ONLY_OWN_TREE_LOOKUP_ERROR(UNAUTHORIZED, "본인의 나무 정보만 조회 가능합니다."),
    ONLY_FRIEND_TREE_LOOKUP_ERROR(UNAUTHORIZED, "친구의 나무만 조회 가능합니다.");

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

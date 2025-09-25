package com.gdg.Todak.common.exception.errors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
public enum GuestbookError implements ErrorInfo {

    GUESTBOOK_NOT_FOUND_ERROR(NOT_FOUND, "존재하지 않는 방명록 입니다."),
    MEMBER_NOT_FOUND_ERROR(NOT_FOUND, "멤버가 존재하지 않습니다."),

    NOT_ALLOWED_TO_LOOK_UP_GUESTBOOK_ERROR(UNAUTHORIZED, "해당 방명록을 조회할 권한이 없습니다. 본인이거나 친구일 경우에만 조회가 가능합니다."),
    NOT_ALLOWED_TO_WRITE_GUESTBOOK_ERROR(UNAUTHORIZED, "해당 방명록에 작성할 권한이 없습니다. 본인이거나 친구일 경우에만 작성이 가능합니다."),
    NOT_GUESTBOOK_OWNER_ERROR(UNAUTHORIZED, "방명록 주인이 아닙니다.");

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

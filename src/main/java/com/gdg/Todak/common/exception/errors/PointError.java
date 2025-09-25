package com.gdg.Todak.common.exception.errors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
public enum PointError implements ErrorInfo {

    POINT_LACK_ERROR(BAD_REQUEST, "요청하신 민큼 포인트를 사용할 수 없습니다."),
    INVALID_GROWTH_BUTTON_ERROR(BAD_REQUEST, "올바른 growthButton이 아닙니다."),
    INVALID_POINT_TYPE_ERROR(BAD_REQUEST, "해당하는 pointType이 없습니다"),
    INVALID_LEVEL_ERROR(BAD_REQUEST, "올바른 level이 아닙니다."),

    ALREADY_HAVE_POINT_OBJECT_ERROR(CONFLICT, "이미 해당 멤버의 point 객체가 존재합니다."),

    POINT_OBJECT_NOT_FOUND_ERROR(NOT_FOUND, "member의 point 객체가 없습니다."),
    USER_NOT_FOUND_ERROR(NOT_FOUND, "userId에 해당하는 멤버가 없습니다."),

    POINT_LOG_UPLOAD_ERROR(INTERNAL_SERVER_ERROR, "포인트 로그 업로드를 실패했습니다."),
    LOCK_ERROR_LOG_UPLOAD_ERROR(INTERNAL_SERVER_ERROR, "락 에러 로그 업로드를 실패했습니다.");

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

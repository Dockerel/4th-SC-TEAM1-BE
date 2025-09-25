package com.gdg.Todak.common.exception.errors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
public enum LockError implements ErrorInfo {

    LOCK_ACQUIRE_ERROR(CONFLICT, "락 획득에 실패하였습니다.");

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

package com.gdg.Todak.common.exception;

import com.gdg.Todak.common.exception.errors.ErrorInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public class TodakException extends RuntimeException {

    private final ErrorInfo errorInfo;

    public HttpStatus status() {
        return errorInfo.status();
    }

    public String message() {
        return errorInfo.message();
    }
}

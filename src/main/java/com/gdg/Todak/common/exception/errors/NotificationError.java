package com.gdg.Todak.common.exception.errors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
public enum NotificationError implements ErrorInfo {

    NOTIFICATION_NOT_FOUND_ERROR(NOT_FOUND, "Notification not found"),

    SENDER_AND_RECEIVER_SAME_ERROR(BAD_REQUEST, "Sender and Receiver can't be same"),

    NOT_NOTIFICATION_OWNER_ERROR(UNAUTHORIZED, "Only owner of notification can delete"),

    NOTIFICATION_CREATION_ERROR(INTERNAL_SERVER_ERROR, "알림 생성 중에 문제가 발생했습니다.");

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

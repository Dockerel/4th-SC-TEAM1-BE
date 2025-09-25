package com.gdg.Todak.common.exception.errors;

import org.springframework.http.HttpStatus;

public interface ErrorInfo {

    HttpStatus status();

    String message();
}

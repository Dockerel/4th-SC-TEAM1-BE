package com.gdg.Todak.common.exception;

import com.gdg.Todak.common.domain.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TodakException.class)
    public ApiResponse<String> handleTodakException(TodakException e) {
        return ApiResponse.of(e.status(), e.message());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<Object> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return ApiResponse.of(
                HttpStatus.CONFLICT,
                e.getMostSpecificCause().getMessage()
        );
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Object> bindException(BindException e) {
        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                e.getBindingResult().getAllErrors().get(0).getDefaultMessage()
        );
    }

}

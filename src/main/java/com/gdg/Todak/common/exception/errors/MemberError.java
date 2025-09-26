package com.gdg.Todak.common.exception.errors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
public enum MemberError implements ErrorInfo {

    EMPTY_IMAGE_ERROR(BAD_REQUEST, "이미지가 비어있습니다."),
    TOO_BIG_IMAGE_ERROR(BAD_REQUEST, "파일 크기가 10MB를 초과했습니다."),
    INVALID_IMAGE_FORMAT_ERROR(BAD_REQUEST, "잘못된 형식의 이미지를 업로드하였습니다. (가능한 형식: jpg, png, gif, bmp, webp, svg)"),
    INVALID_ROLE_ERROR(BAD_REQUEST, "잘못된 유저 ROLE입니다."),

    PASSWORD_ENCRYPTION_ERROR(INTERNAL_SERVER_ERROR, "비밀번호 암호화 중에 문제가 발생했습니다."),
    CLAIM_CREATION_ERROR(INTERNAL_SERVER_ERROR, "claim 생성 중에 문제가 발생했습니다."),

    IMAGE_UPLOAD_FAILED_ERROR(CONFLICT, "이미지 업로드를 실패하였습니다."),
    IMAGE_SAVE_FAILED_ERROR(CONFLICT, "프로필 이미지 저장 중 오류가 발생했습니다."),
    INVALID_DIR_ERROR(CONFLICT, "유효하지 않은 폴더 경로입니다"),
    IMAGE_DELETE_ERROR(CONFLICT, "이미지 파일 삭제 중 오류 발생"),

    INVALID_TOKEN_ERROR(UNAUTHORIZED, "토큰이 없거나, 헤더 형식에 맞지 않습니다."),
    PASSWORD_ERROR(UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),
    NAVER_OAUTH_STATE_ERROR(UNAUTHORIZED, "네이버 로그인 중 문제가 발생했습니다."),
    NOT_SOCIAL_ACCOUNT_ERROR(UNAUTHORIZED, "소셜 로그인 계정이 아닙니다."),
    IS_SOCIAL_ACCOUNT_ERROR(UNAUTHORIZED, "소셜 로그인 계정입니다."),

    MEMBER_NOT_FOUND_ERROR(NOT_FOUND, "존재하지 않는 유저정보 입니다."),

    EXPIRED_REFRESH_TOKEN_ERROR(FORBIDDEN, "리프레시 토큰이 없거나, 만료되었습니다.");

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

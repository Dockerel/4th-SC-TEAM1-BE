package com.gdg.Todak.common.exception.errors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
public enum DiaryError implements ErrorInfo {

    SELF_AUTHORED_COMMENT_ERROR(BAD_REQUEST, "본인이 작성한 댓글은 익명 해제가 필요하지 않습니다."),
    CONFLICT_DIARY_ERROR(BAD_REQUEST, "오늘 이미 작성된 일기 또는 감정이 있습니다. 삭제 후 재작성하거나 작성된 일기를 수정해주세요."),
    MONTH_RANGE_ERROR(BAD_REQUEST, "month의 범위는 1~12 입니다."),
    EMPTY_IMAGE_ERROR(BAD_REQUEST, "이미지가 비어있습니다."),
    TOO_BIG_IMAGE_ERROR(BAD_REQUEST, "파일 크기가 10MB를 초과했습니다."),
    INVALID_IMAGE_FORMAT_ERROR(BAD_REQUEST, "잘못된 형식의 이미지를 업로드하였습니다. (가능한 형식: jpg, png, gif, bmp, webp, svg)"),
    INVALID_URL_FORMAT_ERROR(BAD_REQUEST, "입력 url 형식이 잘못되었습니다."),

    UPLOAD_FAILED_ERROR(CONFLICT, "이미지 업로드를 실패하였습니다."),
    DELETE_FAILED_ERROR(CONFLICT, "이미지 삭제를 실패하였습니다."),

    COMMENT_NOT_FOUND_BY_COMMENT_ID_ERROR(NOT_FOUND, "commentId에 해당하는 댓글이 없습니다."),
    DIARY_NOT_FOUND_BY_DIARY_ID_ERROR(NOT_FOUND, "diary id에 해당하는 일기가 없습니다."),
    USER_NOT_FOUND_BY_USER_ID_ERROR(NOT_FOUND, "userId에 해당하는 멤버가 없습니다."),

    NOT_ALLOWED_COMMENT_LOOK_UP_MEMBER_ERROR(UNAUTHORIZED,"해당 일기의 댓글을 조회할 권한이 없습니다. 일기 작성자가 본인이거나, 친구일 경우에만 조회가 가능합니다."),
    NOT_ALLOWED_COMMENT_MEMBER_ERROR(UNAUTHORIZED,"해당 일기에 댓글을 작성할 권한이 없습니다. 본인이거나 친구일 경우에만 작성이 가능합니다."),
    NOT_COMMENT_OWNER_ERROR(UNAUTHORIZED,"해당 댓글을 수정하거나 삭제할 권한이 없습니다."),
    NOT_DIARY_OWNER_ERROR(UNAUTHORIZED,"일기 작성자가 아닙니다."),
    NOT_FRIEND_ERROR(UNAUTHORIZED,"친구만 조회 가능합니다."),
    NOT_ALLOWED_DIARY_LOOK_UP_MEMBER_ERROR(UNAUTHORIZED,"작성자 또는 작성자의 친구만 일기 조회가 가능합니다."),

    AI_COMMENT_JSON_PARSING_ERROR(INTERNAL_SERVER_ERROR,"AI 댓글 JSON 파싱에 실패했습니다.");

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

package com.clayfin.training.minirib.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Request has invalid fields"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid login ID or password"),
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "Access token is missing"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Access token is invalid"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Access token has expired"),
    USER_LOCKED(HttpStatus.FORBIDDEN, "User is locked"),
    USER_INACTIVE(HttpStatus.FORBIDDEN, "User is inactive"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong, please try again later");

    private final HttpStatus httpStatus;
    private final String message;
}

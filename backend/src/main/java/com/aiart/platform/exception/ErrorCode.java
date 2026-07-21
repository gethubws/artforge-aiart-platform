package com.aiart.platform.exception;

public enum ErrorCode {
    BAD_REQUEST(40000, "Bad request"),
    UNAUTHORIZED(40100, "Unauthorized"),
    FORBIDDEN(40300, "Forbidden"),
    TOO_MANY_REQUESTS(42900, "Too many requests"),
    NOT_FOUND(40400, "Resource not found"),
    USER_EXISTS(40901, "Username already exists"),
    LOGIN_FAILED(40101, "Invalid username or password"),
    SD_UNAVAILABLE(50201, "Generation provider is unavailable"),
    FILE_WRITE_FAILED(50002, "File write failed"),
    INTERNAL_ERROR(50000, "Internal server error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}

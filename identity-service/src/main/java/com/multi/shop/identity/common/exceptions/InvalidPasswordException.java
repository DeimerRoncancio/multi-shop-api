package com.multi.shop.identity.common.exceptions;

public class InvalidPasswordException extends RuntimeException {
    private static final String errorCode = "PASSWORD_UNAUTHORIZED";

    public InvalidPasswordException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}

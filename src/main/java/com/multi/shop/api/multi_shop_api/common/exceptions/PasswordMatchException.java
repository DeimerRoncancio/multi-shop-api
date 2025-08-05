package com.multi.shop.api.multi_shop_api.common.exceptions;

public class PasswordMatchException extends RuntimeException {
    private static final String errorCode = "MATCH_PASSWORD";

    public PasswordMatchException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}

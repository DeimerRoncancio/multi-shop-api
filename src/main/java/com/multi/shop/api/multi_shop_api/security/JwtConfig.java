package com.multi.shop.api.multi_shop_api.security;

import javax.crypto.SecretKey;

public class JwtConfig {
    public static final SecretKey SECRET_KEY = JwtSecretKeys.fromBase64(System.getenv("JWT_SECRET"));
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String PREFIX_TOKEN = "Bearer";
    public static final String CONTENT_TYPE = "application/json";
}

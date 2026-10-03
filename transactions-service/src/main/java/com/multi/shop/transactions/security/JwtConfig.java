package com.multi.shop.transactions.security;

import java.security.PublicKey;

public class JwtConfig {
    public static final PublicKey PUBLIC_KEY = JwtKeys.publicKey(System.getenv("JWT_PUBLIC_KEY"));
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String PREFIX_TOKEN = "Bearer";
    public static final String CONTENT_TYPE = "application/json";
}

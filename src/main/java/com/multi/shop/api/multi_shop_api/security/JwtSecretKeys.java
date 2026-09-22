package com.multi.shop.api.multi_shop_api.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public final class JwtSecretKeys {
    private JwtSecretKeys() {}

    public static SecretKey fromBase64(String base64Secret) {
        if (base64Secret == null || base64Secret.isBlank())
            throw new IllegalStateException("JWT_SECRET is not set. Generate one with: node -e \"console.log(require('crypto').randomBytes(32).toString('base64'))\"");

        byte[] secret;
        try {
            secret = Decoders.BASE64.decode(base64Secret.trim());
        } catch (RuntimeException exception) {
            throw new IllegalStateException("JWT_SECRET must be a Base64 value", exception);
        }

        if (secret.length < 32)
            throw new IllegalStateException("JWT_SECRET must have at least 32 bytes (256 bits)");

        return Keys.hmacShaKeyFor(secret);
    }
}

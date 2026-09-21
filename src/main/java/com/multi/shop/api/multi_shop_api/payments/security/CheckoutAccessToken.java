package com.multi.shop.api.multi_shop_api.payments.security;

import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CheckoutAccessToken {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generate() {
        byte[] accessTokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(accessTokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(accessTokenBytes);
    }

    public String digest(String checkoutAccessToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(checkoutAccessToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    public boolean grants(Transaction transaction, String checkoutAccessToken) {
        return matches(transaction.getCheckoutAccessTokenDigest(), checkoutAccessToken);
    }

    public boolean matches(String storedDigest, String checkoutAccessToken) {
        if (storedDigest == null || checkoutAccessToken == null) return false;

        return MessageDigest.isEqual(
            storedDigest.getBytes(StandardCharsets.UTF_8),
            digest(checkoutAccessToken).getBytes(StandardCharsets.UTF_8)
        );
    }
}

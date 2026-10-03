package com.multi.shop.transactions.security;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class JwtKeys {
    private static final String HOW_TO_GENERATE =
        "Generate a pair with: openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -outform DER -out private.der"
            + " && openssl pkey -inform DER -in private.der -pubout -outform DER -out public.der, then save both in Base64";

    private JwtKeys() {}

    public static PublicKey publicKey(String base64) {
        byte[] der = decode("JWT_PUBLIC_KEY", base64);
        try {
            return strongEnough("JWT_PUBLIC_KEY", KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("JWT_PUBLIC_KEY must be an RSA public key (X.509 DER in Base64). " + HOW_TO_GENERATE, exception);
        }
    }

    private static byte[] decode(String name, String base64) {
        if (base64 == null || base64.isBlank())
            throw new IllegalStateException(name + " is not set. " + HOW_TO_GENERATE);

        try {
            return Base64.getDecoder().decode(base64.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(name + " must be a Base64 value", exception);
        }
    }

    private static <K> K strongEnough(String name, K key) {
        if (((RSAKey) key).getModulus().bitLength() < 2048)
            throw new IllegalStateException(name + " must be an RSA key of at least 2048 bits");
        return key;
    }
}

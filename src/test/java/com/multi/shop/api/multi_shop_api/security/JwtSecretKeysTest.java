package com.multi.shop.api.multi_shop_api.security;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtSecretKeysTest {
    private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);

    @Test
    void theSameSecretAlwaysGivesTheSameKey() {
        SecretKey firstStart = JwtSecretKeys.fromBase64(SECRET);
        SecretKey secondStart = JwtSecretKeys.fromBase64(SECRET);

        String token = Jwts.builder().subject("juan@mail.com").signWith(firstStart).compact();

        assertThat(Jwts.parser().verifyWith(secondStart).build().parseSignedClaims(token).getPayload().getSubject())
            .isEqualTo("juan@mail.com");
    }

    @Test
    void refusesToStartWithoutASecret() {
        assertThatThrownBy(() -> JwtSecretKeys.fromBase64(null)).hasMessageContaining("JWT_SECRET is not set");
        assertThatThrownBy(() -> JwtSecretKeys.fromBase64("  ")).hasMessageContaining("JWT_SECRET is not set");
    }

    @Test
    void refusesAShortSecret() {
        String shortSecret = Base64.getEncoder().encodeToString(new byte[16]);

        assertThatThrownBy(() -> JwtSecretKeys.fromBase64(shortSecret)).hasMessageContaining("at least 32 bytes");
    }

    @Test
    void refusesAValueThatIsNotBase64() {
        assertThatThrownBy(() -> JwtSecretKeys.fromBase64("esto no es base64 ***"))
            .hasMessageContaining("Base64");
    }
}

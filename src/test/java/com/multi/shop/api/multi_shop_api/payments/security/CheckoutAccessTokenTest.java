package com.multi.shop.api.multi_shop_api.payments.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutAccessTokenTest {
    private final CheckoutAccessToken checkoutAccessToken = new CheckoutAccessToken();

    @Test
    void generatesDifferentUrlSafeTokens() {
        String first = checkoutAccessToken.generate();
        String second = checkoutAccessToken.generate();

        assertThat(first).hasSize(43).matches("[A-Za-z0-9_-]+");
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void storesADigestThatIsNotTheTokenItself() {
        String token = checkoutAccessToken.generate();

        String digest = checkoutAccessToken.digest(token);

        assertThat(digest).hasSize(43).isNotEqualTo(token);
        assertThat(checkoutAccessToken.digest(token)).isEqualTo(digest);
    }

    @Test
    void matchesOnlyTheTokenThatProducedTheDigest() {
        String token = checkoutAccessToken.generate();
        String digest = checkoutAccessToken.digest(token);

        assertThat(checkoutAccessToken.matches(digest, token)).isTrue();
        assertThat(checkoutAccessToken.matches(digest, "wrong-access-token")).isFalse();
        assertThat(checkoutAccessToken.matches(digest, null)).isFalse();
        assertThat(checkoutAccessToken.matches(null, token)).isFalse();
    }
}

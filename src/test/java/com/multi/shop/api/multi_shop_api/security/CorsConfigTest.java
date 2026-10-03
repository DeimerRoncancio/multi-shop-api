package com.multi.shop.api.multi_shop_api.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorsConfigTest {
    private static final List<String> ORIGINS = List.of("http://localhost:5173", "https://mi-tienda.com");

    @Test
    void onlyLetsTheListedOriginsRead() {
        CorsConfiguration config = CorsConfig.corsConfiguration(ORIGINS);

        assertThat(config.checkOrigin("https://mi-tienda.com")).isEqualTo("https://mi-tienda.com");
        assertThat(config.checkOrigin("http://localhost:5173")).isEqualTo("http://localhost:5173");
        assertThat(config.checkOrigin("https://web-mala.com")).isNull();
    }

    @Test
    void doesNotAllowCredentials() {
        assertThat(CorsConfig.corsConfiguration(ORIGINS).getAllowCredentials()).isFalse();
    }

    @Test
    void keepsTheHeadersAndMethodsTheStoreUses() {
        CorsConfiguration config = CorsConfig.corsConfiguration(ORIGINS);

        assertThat(config.getAllowedHeaders())
            .contains("Authorization", "content-type", "Token", "X-Checkout-Access-Token");
        assertThat(config.getAllowedMethods()).containsExactly("GET", "POST", "PUT", "DELETE");
    }

    @Test
    void refusesAnEmptyListOrAnAsterisk() {
        assertThatThrownBy(() -> CorsConfig.corsConfiguration(List.of()))
            .hasMessageContaining("app.cors.allowed-origins");
        assertThatThrownBy(() -> CorsConfig.corsConfiguration(List.of("*")))
            .hasMessageContaining("app.cors.allowed-origins");
        assertThatThrownBy(() -> CorsConfig.corsConfiguration(null))
            .hasMessageContaining("app.cors.allowed-origins");
    }
}

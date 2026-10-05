package com.multi.shop.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "CATALOG_URL=http://localhost:1",
        "IDENTITY_URL=http://servicio-que-no-existe.invalid:8084",
        "CORS_ALLOWED_ORIGINS=http://localhost:5173"
    }
)
class ServiceUnavailableTest {

    @Autowired
    private WebTestClient client;

    @Test
    void answers503WhenTheServiceIsOff() {
        client.get().uri("/app/products").exchange().expectStatus().isEqualTo(503);
    }

    @Test
    void answers503WhenTheServiceCannotBeFound() {
        client.get().uri("/app/users/me").exchange().expectStatus().isEqualTo(503);
    }

    @Test
    void keepsAnsweringCorsForTheStore() {
        client.get().uri("/app/products")
            .header("Origin", "http://localhost:5173")
            .exchange()
            .expectStatus().isEqualTo(503)
            .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:5173");
    }
}

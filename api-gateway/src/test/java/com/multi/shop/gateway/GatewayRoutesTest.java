package com.multi.shop.gateway;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "CORS_ALLOWED_ORIGINS=http://localhost:5173"
)
class GatewayRoutesTest {

    private static final MockWebServer MONOLITH = new MockWebServer();
    private static final MockWebServer CATALOG = new MockWebServer();
    private static final MockWebServer IDENTITY = new MockWebServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void monolith(DynamicPropertyRegistry registry) throws IOException {
        MONOLITH.start();
        CATALOG.start();
        IDENTITY.start();
        registry.add("IDENTITY_URL", () -> "http://localhost:" + IDENTITY.getPort());
        registry.add("MONOLITH_URL", () -> "http://localhost:" + MONOLITH.getPort());
        registry.add("CATALOG_URL", () -> "http://localhost:" + CATALOG.getPort());
    }

    @AfterAll
    static void stop() throws IOException {
        MONOLITH.shutdown();
        CATALOG.shutdown();
        IDENTITY.shutdown();
    }

    @BeforeEach
    void drainRequests() throws InterruptedException {
        while (MONOLITH.takeRequest(10, TimeUnit.MILLISECONDS) != null) {
        }
        while (CATALOG.takeRequest(10, TimeUnit.MILLISECONDS) != null) {
        }
        while (IDENTITY.takeRequest(10, TimeUnit.MILLISECONDS) != null) {
        }
    }

    @Test
    void sendsCatalogRoutesToTheCatalogService() throws InterruptedException {
        int monolithBefore = MONOLITH.getRequestCount();
        for (int i = 0; i < 6; i++)
            CATALOG.enqueue(new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json").setBody("{\"content\":[]}"));

        client.get().uri("/app/products?page=1&sort=productName").exchange()
            .expectStatus().isOk()
            .expectBody().json("{\"content\":[]}");
        client.get().uri("/app/products/search?query=camisa").exchange().expectStatus().isOk();
        client.get().uri("/app/categories").exchange().expectStatus().isOk();
        client.put().uri("/app/categories/abc").exchange().expectStatus().isOk();
        client.get().uri("/app/variants").exchange().expectStatus().isOk();
        client.delete().uri("/app/variants/abc").exchange().expectStatus().isOk();

        assertThat(CATALOG.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/app/products?page=1&sort=productName");
        assertThat(CATALOG.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/app/products/search?query=camisa");
        assertThat(CATALOG.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/app/categories");
        assertThat(CATALOG.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/app/categories/abc");
        assertThat(CATALOG.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/app/variants");
        assertThat(CATALOG.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/app/variants/abc");
        assertThat(MONOLITH.getRequestCount()).isEqualTo(monolithBefore);
    }

    @Test
    void sendsTheRestOfTheStoreToTheMonolith() throws InterruptedException {
        int catalogBefore = CATALOG.getRequestCount();
        int identityBefore = IDENTITY.getRequestCount();
        for (int i = 0; i < 3; i++)
            MONOLITH.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        client.get().uri("/app/quantity").exchange().expectStatus().isOk();
        client.post().uri("/app/payments/create-transaction").exchange().expectStatus().isOk();
        client.get().uri("/app/productos-viejos").exchange().expectStatus().isOk();

        assertThat(MONOLITH.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/app/quantity");
        assertThat(MONOLITH.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/app/payments/create-transaction");
        assertThat(MONOLITH.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/app/productos-viejos");
        assertThat(CATALOG.getRequestCount()).isEqualTo(catalogBefore);
        assertThat(IDENTITY.getRequestCount()).isEqualTo(identityBefore);
    }

    @Test
    void doesNotPublishTheInternalCatalogRoutes() {
        int catalogBefore = CATALOG.getRequestCount();

        client.get().uri("/internal/products/abc").exchange().expectStatus().isNotFound();
        client.get().uri("/internal/stats").exchange().expectStatus().isNotFound();
        client.get().uri("/internal/accounts?email=a@b.com").exchange().expectStatus().isNotFound();

        assertThat(CATALOG.getRequestCount()).isEqualTo(catalogBefore);
    }

    @Test
    void sendsLoginUsersAndRolesToTheIdentityService() throws InterruptedException {
        int monolithBefore = MONOLITH.getRequestCount();
        for (int i = 0; i < 5; i++)
            IDENTITY.enqueue(new MockResponse().setResponseCode(200).setBody("{\"token\":\"t\"}"));

        client.post().uri("/login").contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"identifier\":\"a@b.com\",\"password\":\"x\"}")
            .exchange().expectStatus().isOk();
        client.get().uri("/app/users").exchange().expectStatus().isOk();
        client.post().uri("/app/users/register").exchange().expectStatus().isOk();
        client.put().uri("/app/users/update/password/abc").exchange().expectStatus().isOk();
        client.get().uri("/app/roles").exchange().expectStatus().isOk();

        assertThat(IDENTITY.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/login");
        assertThat(IDENTITY.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/app/users");
        assertThat(IDENTITY.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/app/users/register");
        assertThat(IDENTITY.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/app/users/update/password/abc");
        assertThat(IDENTITY.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/app/roles");
        assertThat(MONOLITH.getRequestCount()).isEqualTo(monolithBefore);
    }

    @Test
    void sendsApiDocsToTheMonolith() throws InterruptedException {
        MONOLITH.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        client.get().uri("/v3/api-docs").exchange().expectStatus().isOk();

        assertThat(MONOLITH.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/v3/api-docs");
    }

    @Test
    void keepsTheServicesStatusCodes() {
        IDENTITY.enqueue(new MockResponse().setResponseCode(403));
        IDENTITY.enqueue(new MockResponse().setResponseCode(401).setBody("{\"message\":\"no\"}"));

        client.get().uri("/app/users").exchange().expectStatus().isForbidden();
        client.post().uri("/login").contentType(MediaType.APPLICATION_JSON).bodyValue("{}")
            .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void doesNotAnswerRoutesOutsideTheApi() {
        int before = MONOLITH.getRequestCount();
        client.get().uri("/actuator/env").exchange().expectStatus().isNotFound();
        client.get().uri("/otra-cosa").exchange().expectStatus().isNotFound();
        assertThat(MONOLITH.getRequestCount()).isEqualTo(before);
    }

    @Test
    void passesTheSessionAndCheckoutHeadersUntouched() throws InterruptedException {
        IDENTITY.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        client.get().uri("/app/users/me")
            .header("Authorization", "Bearer abc.def.ghi")
            .header("Token", "abc.def.ghi")
            .header("X-Checkout-Access-Token", "acceso-123")
            .exchange().expectStatus().isOk();

        RecordedRequest request = IDENTITY.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer abc.def.ghi");
        assertThat(request.getHeader("Token")).isEqualTo("abc.def.ghi");
        assertThat(request.getHeader("X-Checkout-Access-Token")).isEqualTo("acceso-123");
    }

    @Test
    void passesTheStripeWebhookBodyByteForByte() throws InterruptedException {
        String payload = "{\n  \"id\": \"evt_1\",  \"type\" : \"checkout.session.completed\" ,\"data\":{\"object\":{\"amount_total\":4700000}}}\n";
        MONOLITH.enqueue(new MockResponse().setResponseCode(200).setBody("Success"));

        client.post().uri("/app/payments/webhook")
            .header("Stripe-Signature", "t=1,v1=firma")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .exchange().expectStatus().isOk();

        RecordedRequest request = MONOLITH.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request.getHeader("Stripe-Signature")).isEqualTo("t=1,v1=firma");
        assertThat(request.getBody().readString(StandardCharsets.UTF_8)).isEqualTo(payload);
    }

    @Test
    void passesUploadedPhotosComplete() throws InterruptedException {
        byte[] photo = new byte[3 * 1024 * 1024];
        for (int i = 0; i < photo.length; i++) photo[i] = (byte) (i % 251);
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("images", new ByteArrayResource(photo) {
            @Override
            public String getFilename() {
                return "foto.png";
            }
        }).contentType(MediaType.IMAGE_PNG);
        body.part("productName", "Camisa");
        CATALOG.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        client.post().uri("/app/products")
            .body(BodyInserters.fromMultipartData(body.build()))
            .exchange().expectStatus().isOk();

        RecordedRequest request = CATALOG.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getHeader("Content-Type")).startsWith("multipart/form-data");
        assertThat(request.getBodySize()).isGreaterThan(photo.length);
    }

    @Test
    void answersTheBrowserCorsCheckForTheStore() {
        int before = MONOLITH.getRequestCount();
        client.method(HttpMethod.OPTIONS).uri("/app/payments/create-transaction")
            .header("Origin", "http://localhost:5173")
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "content-type, x-checkout-access-token")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:5173");

        assertThat(MONOLITH.getRequestCount()).isEqualTo(before);
    }

    @Test
    void rejectsOtherWebsites() {
        int before = CATALOG.getRequestCount();
        client.method(HttpMethod.OPTIONS).uri("/app/products")
            .header("Origin", "https://otro-sitio.com")
            .header("Access-Control-Request-Method", "GET")
            .exchange()
            .expectStatus().isForbidden();

        client.get().uri("/app/products")
            .header("Origin", "https://otro-sitio.com")
            .exchange()
            .expectStatus().isForbidden();

        assertThat(CATALOG.getRequestCount()).isEqualTo(before);
    }

    @Test
    void addsTheCorsHeaderOnceAndDoesNotForwardTheOrigin() throws InterruptedException {
        CATALOG.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        client.get().uri("/app/products")
            .header("Origin", "http://localhost:5173")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().values("Access-Control-Allow-Origin",
                values -> assertThat(values).containsExactly("http://localhost:5173"));

        assertThat(CATALOG.takeRequest(1, TimeUnit.SECONDS).getHeader("Origin")).isNull();
    }

    @Test
    void reportsItsHealth() {
        client.get().uri("/actuator/health").exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.status").isEqualTo("UP");
    }
}

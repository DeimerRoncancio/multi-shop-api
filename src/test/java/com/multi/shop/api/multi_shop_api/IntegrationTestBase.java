package com.multi.shop.api.multi_shop_api;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
    "server.port=0",
    "spring.application.name=multishop-test",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=always",
    "spring.jpa.defer-datasource-initialization=true",
    "stripe.key.secret=sk_test_falsa",
    "stripe.key.public=pk_test_falsa",
    "stripe.success.url=http://localhost:5173/cart/success",
    "stripe.cancel.url=http://localhost:5173/cart/cancel",
    "stripe.webhook.secret=whsec_falsa",
    "app.cors.allowed-origins=http://localhost:5173"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
public abstract class IntegrationTestBase {

    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");

    protected static final FakeMediaService MEDIA = new FakeMediaService();
    protected static final FakeCatalogService CATALOG = new FakeCatalogService();

    static {
        MYSQL.start();
        MEDIA.start();
        CATALOG.start();
    }

    @DynamicPropertySource
    static void mediaService(DynamicPropertyRegistry registry) {
        registry.add("media.service.url", MEDIA::url);
        registry.add("catalog.service.url", CATALOG::url);
    }

    @BeforeEach
    void resetMedia() {
        MEDIA.reset();
        CATALOG.reset();
    }
}

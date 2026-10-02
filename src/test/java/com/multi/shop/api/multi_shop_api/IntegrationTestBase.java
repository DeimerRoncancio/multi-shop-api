package com.multi.shop.api.multi_shop_api;

import com.multi.shop.api.multi_shop_api.media.cloudinary.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
    "cloudinary.cloud-name=prueba",
    "cloudinary.api-key=prueba",
    "cloudinary.api-secret=prueba",
    "app.cors.allowed-origins=http://localhost:5173"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
public abstract class IntegrationTestBase {

    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");

    static {
        MYSQL.start();
    }

    @MockitoBean
    protected CloudinaryService cloudinaryService;

    @BeforeEach
    void fakeCloudinary() throws Exception {
        when(cloudinaryService.upload(any())).thenReturn(Map.of(
            "url", "http://res.cloudinary.com/prueba/image/upload/foto.png",
            "public_id", "foto-prueba"
        ));
        when(cloudinaryService.delete(anyString())).thenReturn(Map.of("result", "ok"));
    }
}

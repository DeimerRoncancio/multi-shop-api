package com.multi.shop.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.application.name=catalog-test"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class CatalogIntegrationTest {

    private static final Path CONTRACTS = Path.of("src/test/resources/contracts");
    private static final String UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    private static final String ADMIN = "[{\"authority\":\"ROLE_ADMIN\"},{\"authority\":\"ROLE_USER\"}]";
    private static final String USER = "[{\"authority\":\"ROLE_USER\"}]";
    private static final PrivateKey TEST_PRIVATE_KEY = testPrivateKey();

    private static final byte[] PNG = {
        (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n', 0, 0, 0, 0x0d, 'I', 'H', 'D', 'R',
        0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 0x1f, 0x15, (byte) 0xc4, (byte) 0x89
    };

    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");

    static final FakeMediaService MEDIA = new FakeMediaService();

    static {
        MYSQL.start();
        MEDIA.start();
    }

    @DynamicPropertySource
    static void mediaService(DynamicPropertyRegistry registry) {
        registry.add("media.service.url", MEDIA::url);
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void resetMedia() {
        MEDIA.reset();
    }

    @Test
    void storeResponsesKeepTheirShape() throws Exception {
        String adminToken = token("admin.contrato@example.com", ADMIN);
        MEDIA.skipUploads(1);

        expect("category-created", send(post("/app/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"categoryName\":\"Contrato Ropa\"}"), adminToken).andExpect(status().isCreated()));

        JsonNode created = expect("product-created", send(multipart("/app/products")
            .file(png("images", "trasera.png"))
            .file(png("images", "frontal.png"))
            .param("productName", "Camisa Contrato")
            .param("description", "Camisa de algodón")
            .param("price", "50000")
            .param("categoriesList", "Contrato Ropa"), adminToken).andExpect(status().isOk()));
        assertThat(created.get("productImages")).hasSize(2);

        String productId = jdbc.queryForObject(
            "SELECT id FROM products WHERE product_name = 'Camisa Contrato'", String.class);

        expect("product-list", send(get("/app/products?sort=productName"), null).andExpect(status().isOk()));
        expect("product-detail", send(get("/app/products/" + productId), null).andExpect(status().isOk()));
        expect("category-list", send(get("/app/categories"), null).andExpect(status().isOk()));
        expect("product-search", send(get("/app/products/search?query=contrato"), null).andExpect(status().isOk()));

        expect("product-updated", send(multipart("/app/products/" + productId)
            .file(png("images", "lateral.png"))
            .param("productName", "Camisa Contrato")
            .param("description", "Camisa de algodón")
            .param("price", "55000")
            .param("categoriesList", "Contrato Ropa")
            .param("imagesToRemove", "foto-2")
            .with(request -> {
                request.setMethod("PUT");
                return request;
            }), adminToken).andExpect(status().isCreated()));
        assertThat(MEDIA.deletedImageIds()).containsExactly("foto-2");
        assertThat(imageNamesOf(productId)).containsExactlyInAnyOrder("camisa-contrato-1.png", "camisa-contrato-2.png");

        mvc.perform(get("/internal/products/" + productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productName").value("Camisa Contrato"))
            .andExpect(jsonPath("$.description").value("Camisa de algodón"))
            .andExpect(jsonPath("$.price").value(55000));
        mvc.perform(get("/internal/products").param("ids", productId, "no-existe"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(productId));
        mvc.perform(get("/internal/products/no-existe")).andExpect(status().isNotFound());
        mvc.perform(get("/internal/stats"))
            .andExpect(jsonPath("$.products").value(1))
            .andExpect(jsonPath("$.categories").value(1));

        send(multipart("/app/products")
            .file(png("images", "unica.png"))
            .param("productName", "Gorra Contrato")
            .param("description", "Gorra")
            .param("price", "20000")
            .param("categoriesList", "Contrato Ropa"), adminToken).andExpect(status().isOk());
        String capId = jdbc.queryForObject(
            "SELECT id FROM products WHERE product_name = 'Gorra Contrato'", String.class);
        int imagesBefore = MEDIA.images().size();

        send(delete("/app/products/" + capId), adminToken).andExpect(status().isOk());
        assertThat(MEDIA.deletedImageIds()).containsExactly("foto-2", "foto-5");
        assertThat(MEDIA.images()).hasSize(imagesBefore - 1);
        assertThat(MEDIA.images()).extracting(FakeMediaService.Image::status).containsOnly("CONFIRMED");
    }

    @Test
    void onlyAdminsChangeTheCatalog() throws Exception {
        String body = "{\"categoryName\":\"Contrato Permisos\"}";

        mvc.perform(post("/app/categories").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden());
        send(post("/app/categories").contentType(MediaType.APPLICATION_JSON).content(body), token("cliente@example.com", USER))
            .andExpect(status().isForbidden());
        mvc.perform(post("/app/categories").contentType(MediaType.APPLICATION_JSON).content(body)
                .header("Authorization", "Bearer token.que.no.vale"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("El token es invalido"));
        mvc.perform(get("/app/products/stats")).andExpect(status().isForbidden());
        mvc.perform(get("/app/variants")).andExpect(status().isForbidden());

        send(post("/app/categories").contentType(MediaType.APPLICATION_JSON).content(body), token("admin@example.com", ADMIN))
            .andExpect(status().isCreated());
        mvc.perform(get("/app/categories")).andExpect(status().isOk());
        mvc.perform(get("/app/products")).andExpect(status().isOk());
    }

    @Test
    void validatesLikeTheMonolith() throws Exception {
        String adminToken = token("admin@example.com", ADMIN);
        send(post("/app/categories").contentType(MediaType.APPLICATION_JSON)
            .content("{\"categoryName\":\"Contrato Repetida\"}"), adminToken).andExpect(status().isCreated());

        send(post("/app/categories").contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryName\":\"Contrato Repetida\"}"), adminToken)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.categoryName").exists());

        send(multipart("/app/products")
                .param("productName", "")
                .param("description", "x")
                .param("price", "1000")
                .param("categoriesList", "No Existe"), adminToken)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.productName").exists())
            .andExpect(jsonPath("$.categoriesList").exists());

        mvc.perform(get("/app/products/" + java.util.UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void keepsNothingHalfSavedWhenTheMediaServiceFails() throws Exception {
        String adminToken = token("admin@example.com", ADMIN);
        send(post("/app/categories").contentType(MediaType.APPLICATION_JSON)
            .content("{\"categoryName\":\"Contrato Fallos\"}"), adminToken).andExpect(status().isCreated());

        MEDIA.setRefuseConfirm(true);
        send(productWithPhoto("Producto Sin Confirmar"), adminToken).andExpect(status().isBadGateway());
        MEDIA.setRefuseConfirm(false);
        assertThat(MEDIA.images()).isEmpty();
        assertThat(MEDIA.deletedImageIds()).hasSize(1);

        MEDIA.setDown(true);
        send(productWithPhoto("Producto Sin Media"), adminToken).andExpect(status().isBadGateway());
        MEDIA.setDown(false);

        assertThat(jdbc.queryForObject(
            "SELECT COUNT(*) FROM products WHERE product_name LIKE 'Producto Sin %'", Long.class)).isZero();
    }

    private MockHttpServletRequestBuilder productWithPhoto(String name) {
        return multipart("/app/products")
            .file(png("images", "foto.png"))
            .param("productName", name)
            .param("description", "No se guarda")
            .param("price", "1000")
            .param("categoriesList", "Contrato Fallos");
    }

    private static PrivateKey testPrivateKey() {
        try {
            byte[] der = Base64.getDecoder().decode(System.getenv("JWT_TEST_PRIVATE_KEY"));
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String token(String subject, String authorities) {
        return Jwts.builder()
            .subject(subject)
            .claim("authorities", authorities)
            .expiration(new Date(System.currentTimeMillis() + 3600000))
            .issuedAt(new Date())
            .signWith(TEST_PRIVATE_KEY)
            .compact();
    }

    private ResultActions send(MockHttpServletRequestBuilder request, String token) throws Exception {
        if (token != null) request.header("Authorization", "Bearer " + token);
        return mvc.perform(request);
    }

    private JsonNode expect(String name, ResultActions result) throws Exception {
        String body = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String actual = body.replaceAll(UUID, "<uuid>");
        Path file = CONTRACTS.resolve(name + ".json");

        if (Files.notExists(file)) {
            Files.createDirectories(CONTRACTS);
            Files.writeString(file, mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(mapper.readTree(actual)) + System.lineSeparator());
        }

        JSONAssert.assertEquals(name, Files.readString(file), actual, JSONCompareMode.STRICT);
        return mapper.readTree(body);
    }

    private List<String> imageNamesOf(String productId) {
        return jdbc.queryForList("SELECT id_image FROM images_to_products WHERE id_product = ?", String.class, productId)
            .stream()
            .map(id -> MEDIA.image(id).orElseThrow().name())
            .toList();
    }

    private static MockMultipartFile png(String field, String filename) {
        return new MockMultipartFile(field, filename, "image/png", PNG);
    }
}

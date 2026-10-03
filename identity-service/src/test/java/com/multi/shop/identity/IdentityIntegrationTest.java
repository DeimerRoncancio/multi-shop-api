package com.multi.shop.identity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
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
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.application.name=identity-test",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=always",
    "spring.jpa.defer-datasource-initialization=true"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class IdentityIntegrationTest {

    private static final Path CONTRACTS = Path.of("src/test/resources/contracts");
    private static final String UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    private static final byte[] PNG = {
        (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n', 0, 0, 0, 0x0d, 'I', 'H', 'D', 'R',
        0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 0x1f, 0x15, (byte) 0xc4, (byte) 0x89
    };

    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");

    static final FakeMediaService MEDIA = new FakeMediaService();
    static final MockWebServer CATALOG = new MockWebServer();
    static volatile boolean catalogDown;

    static {
        MYSQL.start();
        MEDIA.start();
        CATALOG.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (catalogDown) return new MockResponse().setResponseCode(503);
                if (!"/internal/stats".equals(request.getPath())) return new MockResponse().setResponseCode(404);
                return new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody("{\"products\":15,\"categories\":6}");
            }
        });
        try {
            CATALOG.start();
        } catch (java.io.IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @DynamicPropertySource
    static void mediaService(DynamicPropertyRegistry registry) {
        registry.add("media.service.url", MEDIA::url);
        registry.add("catalog.service.url", () -> "http://localhost:" + CATALOG.getPort());
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
        String adminToken = registerAdmin();
        MEDIA.skipUploads(3);

        String userToken = registerUser("Bruno", "bruno.contrato@example.com", "3009990001");
        JsonNode me = expect("user-me", send(get("/app/users/me").header("Token", userToken), userToken)
            .andExpect(status().isOk()));
        String userId = me.get("id").asText();

        expect("user-profile-image", send(multipart("/app/users/update/profile-image/" + userId)
            .file(png("file", "nueva.png"))
            .with(request -> {
                request.setMethod("PUT");
                return request;
            }), userToken).andExpect(status().isCreated()));
        assertThat(MEDIA.deletedImageIds()).containsExactly("foto-5");

        expect("user-search", send(get("/app/users/search?identifier=contrato&isAdmin=false&field=EMAIL"), adminToken)
            .andExpect(status().isOk()));

        mvc.perform(get("/internal/accounts").param("identity", "bruno.contrato@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Bruno"))
            .andExpect(jsonPath("$.email").value("bruno.contrato@example.com"))
            .andExpect(jsonPath("$.phoneNumber").value(3009990001L));
        mvc.perform(get("/internal/accounts").param("identity", "3009990001"))
            .andExpect(jsonPath("$.email").value("bruno.contrato@example.com"));
        mvc.perform(get("/internal/accounts").param("email", "bruno.contrato@example.com"))
            .andExpect(jsonPath("$.name").value("Bruno"));
        mvc.perform(get("/internal/accounts").param("email", "nadie@example.com")).andExpect(status().isNotFound());
        mvc.perform(get("/internal/accounts")).andExpect(status().isNotFound());
        long users = jdbc.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        mvc.perform(get("/internal/stats")).andExpect(jsonPath("$.users").value(users));

        MEDIA.skipUploads(1);
        String carlaToken = registerUser("Carla", "carla.contrato@example.com", "3009990002");
        String carlaId = jdbc.queryForObject(
            "SELECT id FROM users WHERE email = 'carla.contrato@example.com'", String.class);
        send(delete("/app/users/" + carlaId), carlaToken).andExpect(status().isOk());

        assertThat(MEDIA.images()).extracting(FakeMediaService.Image::status).containsOnly("CONFIRMED");
        assertThat(MEDIA.deletedImageIds()).containsExactly("foto-5", "foto-8");
    }

    @Test
    void quantityJoinsUsersAndTheCatalog() throws Exception {
        String adminToken = registerAdmin("Cuenta", "admin.cuentas@example.com", "3009990020");
        registerUser("Mora", "mora.cuentas@example.com", "3009990021");
        String userToken = login("mora.cuentas@example.com");
        long users = jdbc.queryForObject("SELECT COUNT(*) FROM users", Long.class);

        send(get("/app/quantity"), adminToken)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.users").value(users))
            .andExpect(jsonPath("$.products").value(15))
            .andExpect(jsonPath("$.categories").value(6));

        mvc.perform(get("/app/quantity")).andExpect(status().isForbidden());
        send(get("/app/quantity"), userToken).andExpect(status().isForbidden());

        catalogDown = true;
        try {
            send(get("/app/quantity"), adminToken).andExpect(status().isBadGateway());
        } finally {
            catalogDown = false;
        }
    }

    @Test
    void signsTokensWithRs256AndRejectsTheOldSharedSecret() throws Exception {
        registerUser("Rosa", "rosa.tokens@example.com", "3009990010");
        String token = login("rosa.tokens@example.com");

        String header = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]), StandardCharsets.UTF_8);
        assertThat(mapper.readTree(header).get("alg").asText()).isEqualTo("RS256");
        mvc.perform(get("/app/users/token-validation").header("Token", token)).andExpect(status().isOk());

        String oldStyle = Jwts.builder()
            .subject("rosa.tokens@example.com")
            .claim("authorities", "[{\"authority\":\"ROLE_ADMIN\"}]")
            .expiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode("c29sby1wYXJhLXBydWViYXMtbm8tZXMtc2VjcmV0byEh")))
            .compact();

        mvc.perform(get("/app/users").header("Authorization", "Bearer " + oldStyle))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("El token es invalido"));
        mvc.perform(get("/app/users/token-validation").header("Token", oldStyle)).andExpect(status().isBadRequest());
    }

    @Test
    void registerLoginAndReadOwnProfile() throws Exception {
        mvc.perform(multipart("/app/users/register")
                .file(png("profileImage", "foto.png"))
                .param("name", "Ana")
                .param("lastnames", "Prueba")
                .param("email", "ana@example.com")
                .param("phoneNumber", "3001234567")
                .param("password", "ClaveSegura123")
                .param("admin", "false"))
            .andExpect(status().isCreated());

        String token = login("ana@example.com");

        mvc.perform(get("/app/users/me")
                .header("Authorization", "Bearer " + token)
                .header("Token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void onlyAdminsListUsers() throws Exception {
        registerUser("Pedro", "pedro.permisos@example.com", "3009990011");
        String userToken = login("pedro.permisos@example.com");

        mvc.perform(get("/app/users")).andExpect(status().isForbidden());
        send(get("/app/users"), userToken).andExpect(status().isForbidden());
        send(get("/app/roles"), userToken).andExpect(status().isForbidden());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"nadie@example.com\",\"password\":\"ClaveSegura123\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("Content-Type", "application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.message").value("Error en la autenticación. Usuario o contraseña incorrectos."));
    }

    @Test
    void invalidTokenMessageIsUtf8() throws Exception {
        mvc.perform(get("/app/users/me")
                .header("Authorization", "Bearer token.que.no.vale")
                .header("Token", "token.que.no.vale"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("Content-Type", "application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.message").value("El token es invalido"));
    }

    @Test
    void keepsNothingHalfSavedWhenTheMediaServiceFails() throws Exception {
        MEDIA.setRefuseConfirm(true);
        mvc.perform(multipart("/app/users/register")
                .file(png("profileImage", "perfil.png"))
                .param("name", "Fallos")
                .param("lastnames", "Contrato")
                .param("email", "fallos.contrato@example.com")
                .param("phoneNumber", "3009990003")
                .param("password", "ClaveSegura123")
                .param("admin", "false"))
            .andExpect(status().isBadGateway());
        MEDIA.setRefuseConfirm(false);

        assertThat(jdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE email = 'fallos.contrato@example.com'", Long.class)).isZero();
        assertThat(MEDIA.images()).isEmpty();
        assertThat(MEDIA.deletedImageIds()).hasSize(1);
    }

    private String registerAdmin() throws Exception {
        return registerAdmin("Admin", "admin.contrato@example.com", "3009990000");
    }

    private String registerAdmin(String name, String email, String phone) throws Exception {
        registerUser(name, email, phone);
        String id = jdbc.queryForObject("SELECT id FROM users WHERE email = ?", String.class, email);
        jdbc.update("UPDATE users SET admin = true WHERE id = ?", id);
        jdbc.update("INSERT INTO roles_to_users (id_user, id_role) VALUES (?, 'rol-admin')", id);
        return login(email);
    }

    private String registerUser(String name, String email, String phone) throws Exception {
        MockMultipartHttpServletRequestBuilder request = multipart("/app/users/register");
        request.file(png("profileImage", "perfil.png"));
        request.param("name", name)
            .param("lastnames", "Contrato")
            .param("email", email)
            .param("phoneNumber", phone)
            .param("password", "ClaveSegura123")
            .param("admin", "false");

        ResultActions result = mvc.perform(request).andExpect(status().isCreated());
        if (email.endsWith(".contrato@example.com")) expect("user-registered-" + name.toLowerCase(), result);
        return login(email);
    }

    private String login(String email) throws Exception {
        String body = mvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"" + email + "\",\"password\":\"ClaveSegura123\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("token").asText();
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

    private static MockMultipartFile png(String field, String filename) {
        return new MockMultipartFile(field, filename, "image/png", PNG);
    }
}

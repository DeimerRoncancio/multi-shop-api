package com.multi.shop.api.multi_shop_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiContractIntegrationTest extends IntegrationTestBase {

    private static final Path CONTRACTS = Path.of("src/test/resources/contracts");
    private static final String UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    private static final byte[] PNG = {
        (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n', 0, 0, 0, 0x0d, 'I', 'H', 'D', 'R',
        0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 0x1f, 0x15, (byte) 0xc4, (byte) 0x89
    };

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JdbcTemplate jdbc;

    private final AtomicInteger uploads = new AtomicInteger();

    @BeforeEach
    void numberedUploads() throws Exception {
        when(cloudinaryService.upload(any())).thenAnswer(invocation -> {
            int number = uploads.incrementAndGet();
            return Map.of(
                "url", "http://res.cloudinary.com/prueba/image/upload/foto-" + number + ".png",
                "public_id", "foto-" + number
            );
        });
    }

    @Test
    void storeResponsesKeepTheirShape() throws Exception {
        String adminToken = registerAdmin();

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
        verify(cloudinaryService).delete("foto-2");
        assertThat(imageNamesOf(productId)).containsExactlyInAnyOrder("camisa-contrato-1.png", "camisa-contrato-2.png");

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
        verify(cloudinaryService).delete("foto-5");

        expect("user-search", send(get("/app/users/search?identifier=contrato&isAdmin=false&field=EMAIL"), adminToken).andExpect(status().isOk()));

        JsonNode guestAccess = expect("transaction-created", send(post("/app/payments/create-transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"productItems\":[{\"id\":\"" + productId + "\",\"quantity\":2}]}"), null)
            .andExpect(status().isOk()));
        String guestTransaction = guestAccess.get("transactionId").asText();
        String guestAccessToken = guestAccess.get("checkoutAccessToken").asText();

        send(put("/app/payments/add-user/" + guestTransaction)
            .header("X-Checkout-Access-Token", guestAccessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(customer("Invitada Contrato", "invitada@example.com")), null)
            .andExpect(status().isCreated());
        expect("checkout-guest", send(get("/app/payments/checkout/" + guestTransaction)
            .header("X-Checkout-Access-Token", guestAccessToken), null).andExpect(status().isOk()));

        JsonNode userAccess = mapper.readTree(send(post("/app/payments/create-transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"productItems\":[{\"id\":\"" + productId + "\",\"quantity\":1}]}"), userToken)
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        String userTransaction = userAccess.get("transactionId").asText();
        String userAccessToken = userAccess.get("checkoutAccessToken").asText();

        send(put("/app/payments/add-user/" + userTransaction)
            .header("X-Checkout-Access-Token", userAccessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(customer("Bruno", "bruno.contrato@example.com")), userToken)
            .andExpect(status().isCreated());
        expect("checkout-user", send(get("/app/payments/checkout/" + userTransaction)
            .header("X-Checkout-Access-Token", userAccessToken), null).andExpect(status().isOk()));
        expect("saved-addresses", send(get("/app/payments/saved-addresses"), userToken).andExpect(status().isOk()));

        send(put("/app/payments/update-products/" + userTransaction)
            .header("X-Checkout-Access-Token", userAccessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("[{\"id\":\"" + productId + "\",\"quantity\":3}]"), null)
            .andExpect(status().isCreated());
        assertThat(jdbc.queryForObject("SELECT total_price FROM transactions WHERE id = ?", Long.class, userTransaction))
            .isEqualTo(165000L);

        jdbc.update("UPDATE products SET price = 99000 WHERE id = ?", productId);
        JsonNode frozen = mapper.readTree(send(get("/app/payments/checkout/" + userTransaction)
            .header("X-Checkout-Access-Token", userAccessToken), null)
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(frozen.get("totalPrice").asLong()).isEqualTo(165000L);
        assertThat(frozen.get("items").get(0).get("price").asLong()).isEqualTo(55000L);
        assertThat(frozen.get("items").get(0).get("productName").asText()).isEqualTo("Camisa Contrato");
        jdbc.update("UPDATE products SET price = 55000 WHERE id = ?", productId);

        JsonNode quantity = mapper.readTree(send(get("/app/quantity"), adminToken).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        assertThat(quantity.fieldNames()).toIterable().containsExactlyInAnyOrder("users", "products", "categories");
        assertThat(quantity.get("products").asLong()).isEqualTo(
            jdbc.queryForObject("SELECT COUNT(*) FROM products", Long.class));

        send(multipart("/app/products")
            .file(png("images", "unica.png"))
            .param("productName", "Gorra Contrato")
            .param("description", "Gorra")
            .param("price", "20000")
            .param("categoriesList", "Contrato Ropa"), adminToken).andExpect(status().isOk());
        String capId = jdbc.queryForObject(
            "SELECT id FROM products WHERE product_name = 'Gorra Contrato'", String.class);
        long imagesBefore = jdbc.queryForObject("SELECT COUNT(*) FROM images", Long.class);

        send(delete("/app/products/" + capId), adminToken).andExpect(status().isOk());
        verify(cloudinaryService).delete("foto-7");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM images", Long.class)).isEqualTo(imagesBefore - 1);

        String carlaToken = registerUser("Carla", "carla.contrato@example.com", "3009990002");
        String carlaId = jdbc.queryForObject(
            "SELECT id FROM users WHERE email = 'carla.contrato@example.com'", String.class);
        send(delete("/app/users/" + carlaId), carlaToken).andExpect(status().isOk());
        verify(cloudinaryService).delete("foto-8");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM images WHERE image_id = 'foto-8'", Long.class)).isZero();
    }

    private String registerAdmin() throws Exception {
        registerUser("Admin", "admin.contrato@example.com", "3009990000");
        String id = jdbc.queryForObject("SELECT id FROM users WHERE email = 'admin.contrato@example.com'", String.class);
        jdbc.update("UPDATE users SET admin = true WHERE id = ?", id);
        jdbc.update("INSERT INTO roles_to_users (id_user, id_role) VALUES (?, 'rol-admin')", id);
        return login("admin.contrato@example.com");
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

        JsonNode registered = expect("user-registered-" + name.toLowerCase(), mvc.perform(request)
            .andExpect(status().isCreated()));
        assertThat(registered.get("email").asText()).isEqualTo(email);
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
        String actual = normalize(body);
        Path file = CONTRACTS.resolve(name + ".json");

        if (Files.notExists(file)) {
            Files.createDirectories(CONTRACTS);
            Files.writeString(file, mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(mapper.readTree(actual)) + System.lineSeparator());
        }

        JSONAssert.assertEquals(name, Files.readString(file), actual, JSONCompareMode.STRICT);
        return mapper.readTree(body);
    }

    private static String normalize(String json) {
        return json.replaceAll(UUID, "<uuid>")
            .replaceAll("\"checkoutAccessToken\":\"[^\"]+\"", "\"checkoutAccessToken\":\"<token>\"");
    }

    private java.util.List<String> imageNamesOf(String productId) {
        return jdbc.queryForList("""
            SELECT i.name FROM images i
            JOIN images_to_products ip ON ip.id_image = i.id
            WHERE ip.id_product = ?
            """, String.class, productId);
    }

    private static String customer(String names, String email) {
        return """
            {"userNames":"%s","userEmail":"%s","userPhone":"3001112233",
             "userAddress":{"addressName":"Casa","address":"Calle 1 # 2-3","city":"Bogotá",
             "state":"Cundinamarca","country":"Colombia","addressNumber":"101"}}
            """.formatted(names, email);
    }

    private static MockMultipartFile png(String field, String filename) {
        return new MockMultipartFile(field, filename, "image/png", PNG);
    }
}

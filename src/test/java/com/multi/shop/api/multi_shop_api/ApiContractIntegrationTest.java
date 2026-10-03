package com.multi.shop.api.multi_shop_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiContractIntegrationTest extends IntegrationTestBase {

    private static final Path CONTRACTS = Path.of("src/test/resources/contracts");
    private static final String UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void storeResponsesKeepTheirShape() throws Exception {
        String adminToken = FakeIdentityService.adminToken("admin.contrato@example.com");
        String userToken = FakeIdentityService.userToken("bruno.contrato@example.com");
        IDENTITY.addAccount("Admin", "admin.contrato@example.com", 3009990000L);
        IDENTITY.addAccount("Bruno", "bruno.contrato@example.com", 3009990001L);
        String productId = CATALOG.addProduct("Camisa Contrato", "Camisa de algodón", 55000L).id();
        CATALOG.setCategories(1);

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

        CATALOG.changePrice(productId, 99000L);
        JsonNode frozen = mapper.readTree(send(get("/app/payments/checkout/" + userTransaction)
            .header("X-Checkout-Access-Token", userAccessToken), null)
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(frozen.get("totalPrice").asLong()).isEqualTo(165000L);
        assertThat(frozen.get("items").get(0).get("price").asLong()).isEqualTo(55000L);
        assertThat(frozen.get("items").get(0).get("productName").asText()).isEqualTo("Camisa Contrato");
        CATALOG.changePrice(productId, 55000L);

        JsonNode quantity = mapper.readTree(send(get("/app/quantity"), adminToken).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        assertThat(quantity.fieldNames()).toIterable().containsExactlyInAnyOrder("users", "products", "categories");
        assertThat(quantity.get("users").asLong()).isEqualTo(2L);
        assertThat(quantity.get("products").asLong()).isEqualTo(1L);
        assertThat(quantity.get("categories").asLong()).isEqualTo(1L);
        send(get("/app/quantity"), userToken).andExpect(status().isForbidden());
    }

    @Test
    void purchasesNeedTheCatalogService() throws Exception {
        String productId = CATALOG.addProduct("Gorra Contrato", "Gorra", 20000L).id();

        send(post("/app/payments/create-transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"productItems\":[{\"id\":\"no-existe\",\"quantity\":1}]}"), null)
            .andExpect(status().isBadRequest());

        CATALOG.setDown(true);
        send(post("/app/payments/create-transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"productItems\":[{\"id\":\"" + productId + "\",\"quantity\":1}]}"), null)
            .andExpect(status().isBadGateway());
        CATALOG.setDown(false);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM product_items WHERE product_id = ?", Long.class, productId))
            .isZero();
    }

    @Test
    void signedInPurchasesNeedTheIdentityService() throws Exception {
        String userToken = FakeIdentityService.userToken("diana.contrato@example.com");
        IDENTITY.addAccount("Diana", "diana.contrato@example.com", 3009990004L);
        String productId = CATALOG.addProduct("Bolso Contrato", "Bolso", 30000L).id();
        JsonNode access = mapper.readTree(send(post("/app/payments/create-transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"productItems\":[{\"id\":\"" + productId + "\",\"quantity\":1}]}"), userToken)
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        IDENTITY.setDown(true);
        send(put("/app/payments/add-user/" + access.get("transactionId").asText())
            .header("X-Checkout-Access-Token", access.get("checkoutAccessToken").asText())
            .contentType(MediaType.APPLICATION_JSON)
            .content(customer("Diana", "diana.contrato@example.com")), userToken)
            .andExpect(status().isBadGateway());
        send(get("/app/payments/saved-addresses"), userToken).andExpect(status().isBadGateway());
        IDENTITY.setDown(false);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transactions WHERE id = ? AND customer_id IS NULL",
            Long.class, access.get("transactionId").asText())).isEqualTo(1L);
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

    private static String customer(String names, String email) {
        return """
            {"userNames":"%s","userEmail":"%s","userPhone":"3001112233",
             "userAddress":{"addressName":"Casa","address":"Calle 1 # 2-3","city":"Bogotá",
             "state":"Cundinamarca","country":"Colombia","addressNumber":"101"}}
            """.formatted(names, email);
    }
}

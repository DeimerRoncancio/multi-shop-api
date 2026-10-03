package com.multi.shop.api.multi_shop_api;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FrontendRoutesIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mvc;

    @Test
    void apiDocsArePublished() throws Exception {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/app/payments/create-transaction']").exists());
    }

    @Test
    void privateRoutesNeedAToken() throws Exception {
        mvc.perform(get("/app/quantity")).andExpect(status().isForbidden());
        mvc.perform(get("/app/payments/saved-addresses")).andExpect(status().isForbidden());
    }

    @Test
    void invalidTokenMessageIsUtf8() throws Exception {
        mvc.perform(get("/app/payments/saved-addresses")
                .header("Authorization", "Bearer token.que.no.vale"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("Content-Type", "application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.message").value("El token es invalido"));
    }

    @Test
    void rejectsTokensSignedWithTheOldSharedSecret() throws Exception {
        String oldStyle = Jwts.builder()
            .subject("admin@example.com")
            .claim("authorities", "[{\"authority\":\"ROLE_ADMIN\"}]")
            .expiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode("c29sby1wYXJhLXBydWViYXMtbm8tZXMtc2VjcmV0byEh")))
            .compact();

        mvc.perform(get("/app/quantity").header("Authorization", "Bearer " + oldStyle))
            .andExpect(status().isUnauthorized());
    }
}

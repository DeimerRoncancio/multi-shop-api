package com.multi.shop.api.multi_shop_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FrontendRoutesIntegrationTest extends IntegrationTestBase {

    private static final byte[] PNG = {
        (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n', 0, 0, 0, 0x0d, 'I', 'H', 'D', 'R',
        0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 0x1f, 0x15, (byte) 0xc4, (byte) 0x89
    };

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void catalogIsPublic() throws Exception {
        mvc.perform(get("/app/products")).andExpect(status().isOk());
        mvc.perform(get("/app/categories")).andExpect(status().isOk());
    }

    @Test
    void apiDocsArePublished() throws Exception {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/app/products']").exists());
    }

    @Test
    void privateRoutesNeedAToken() throws Exception {
        mvc.perform(get("/app/users")).andExpect(status().isForbidden());
        mvc.perform(get("/app/quantity")).andExpect(status().isForbidden());
    }

    @Test
    void registerLoginAndReadOwnProfile() throws Exception {
        MockMultipartFile photo = new MockMultipartFile("profileImage", "foto.png", "image/png", PNG);

        mvc.perform(multipart("/app/users/register")
                .file(photo)
                .param("name", "Ana")
                .param("lastnames", "Prueba")
                .param("email", "ana@example.com")
                .param("phoneNumber", "3001234567")
                .param("password", "ClaveSegura123")
                .param("admin", "false"))
            .andExpect(status().isCreated());

        String body = mvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"ana@example.com\",\"password\":\"ClaveSegura123\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode login = mapper.readTree(body);
        String token = login.get("token").asText();

        mvc.perform(get("/app/users/me")
                .header("Authorization", "Bearer " + token)
                .header("Token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"nadie@example.com\",\"password\":\"ClaveSegura123\"}"))
            .andExpect(status().isUnauthorized());
    }
}

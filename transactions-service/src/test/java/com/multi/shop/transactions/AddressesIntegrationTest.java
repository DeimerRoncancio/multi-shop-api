package com.multi.shop.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multi.shop.transactions.repositories.CustomersRepository;
import com.multi.shop.transactions.repositories.PaymentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AddressesIntegrationTest extends IntegrationTestBase {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PaymentsRepository payments;

    @Autowired
    private CustomersRepository customers;

    private String ana;
    private String luis;

    @BeforeEach
    void setUp() {
        payments.deleteAll();
        customers.deleteAll();
        IDENTITY.addAccount("Ana", "ana@correo.com", 3001234567L);
        IDENTITY.addAccount("Luis", "luis@correo.com", 3007654321L);
        ana = "Bearer " + FakeIdentityService.userToken("ana@correo.com");
        luis = "Bearer " + FakeIdentityService.userToken("luis@correo.com");
    }

    @Test
    void usersManageTheirAddresses() throws Exception {
        String id = create(ana, "Oficina", "Carrera 7 # 72");
        create(ana, "Casa", "Calle 14 # 2");

        mvc.perform(get("/app/addresses").header("Authorization", ana))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].addressName").value("Casa"))
            .andExpect(jsonPath("$[1].addressName").value("Oficina"));

        mvc.perform(put("/app/addresses/" + id).header("Authorization", ana)
                .contentType("application/json").content(body("Trabajo", "Carrera 9 # 10")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.addressName").value("Trabajo"))
            .andExpect(jsonPath("$.address").value("Carrera 9 # 10"));

        mvc.perform(delete("/app/addresses/" + id).header("Authorization", ana))
            .andExpect(status().isNoContent());

        mvc.perform(get("/app/addresses").header("Authorization", ana))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].addressName").value("Casa"));
    }

    @Test
    void rejectsRepeatedNames() throws Exception {
        create(ana, "Casa", "Calle 14 # 2");
        String id = create(ana, "Oficina", "Carrera 7 # 72");

        mvc.perform(post("/app/addresses").header("Authorization", ana)
                .contentType("application/json").content(body("Casa", "Otra calle")))
            .andExpect(status().isConflict());

        mvc.perform(put("/app/addresses/" + id).header("Authorization", ana)
                .contentType("application/json").content(body("Casa", "Carrera 7 # 72")))
            .andExpect(status().isConflict());
    }

    @Test
    void usersCannotTouchOtherAddresses() throws Exception {
        String id = create(ana, "Casa", "Calle 14 # 2");

        mvc.perform(put("/app/addresses/" + id).header("Authorization", luis)
                .contentType("application/json").content(body("Mia", "Calle 1")))
            .andExpect(status().isNotFound());
        mvc.perform(delete("/app/addresses/" + id).header("Authorization", luis))
            .andExpect(status().isNotFound());
        mvc.perform(get("/app/addresses").header("Authorization", luis))
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addressesNeedATokenAndValidData() throws Exception {
        mvc.perform(get("/app/addresses")).andExpect(status().isForbidden());

        mvc.perform(post("/app/addresses").header("Authorization", ana)
                .contentType("application/json").content("{\"addressName\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    private String create(String token, String name, String address) throws Exception {
        String response = mvc.perform(post("/app/addresses").header("Authorization", token)
                .contentType("application/json").content(body(name, address)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JSON.readTree(response).get("id").asText();
    }

    private static String body(String name, String address) {
        return """
            {"addressName":"%s","address":"%s","city":"Bogota","state":"Cundinamarca","country":"Colombia","addressNumber":"3001234567"}
            """.formatted(name, address);
    }
}

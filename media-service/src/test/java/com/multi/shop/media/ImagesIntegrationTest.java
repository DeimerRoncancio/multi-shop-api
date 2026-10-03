package com.multi.shop.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multi.shop.media.cloudinary.CloudinaryService;
import com.multi.shop.media.cloudinary.CloudinaryService.UploadedFile;
import com.multi.shop.media.images.ImageCleanup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "cloudinary.cloud-name=prueba",
    "cloudinary.api-key=prueba",
    "cloudinary.api-secret=prueba"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ImagesIntegrationTest {

    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");

    static {
        MYSQL.start();
    }

    @MockitoBean
    private CloudinaryService cloudinary;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ImageCleanup cleanup;

    private final AtomicInteger uploads = new AtomicInteger();

    @BeforeEach
    void fakeCloudinary() throws IOException {
        jdbc.update("DELETE FROM images");
        reset(cloudinary);
        when(cloudinary.upload(any())).thenAnswer(invocation -> {
            int number = uploads.incrementAndGet();
            return new UploadedFile("http://res.cloudinary.com/prueba/foto-" + number + ".png", "foto-" + number);
        });
    }

    @Test
    void uploadsToCloudinaryAndKeepsTheImagePending() throws Exception {
        JsonNode image = upload("camisa.png");

        assertThat(image.get("name").asText()).isEqualTo("camisa.png");
        assertThat(image.get("imageUrl").asText()).startsWith("http://res.cloudinary.com/prueba/foto-");
        assertThat(image.get("imageId").asText()).startsWith("foto-");
        assertThat(statusOf(image)).isEqualTo("PENDING");
    }

    @Test
    void refusesAnEmptyFile() throws Exception {
        mvc.perform(multipart("/images").file(new MockMultipartFile("file", "vacia.png", "image/png", new byte[0])))
            .andExpect(status().isBadRequest());
        assertThat(count()).isZero();
    }

    @Test
    void savesNothingWhenCloudinaryFails() throws Exception {
        when(cloudinary.upload(any())).thenThrow(new IOException("cloudinary caída"));

        mvc.perform(multipart("/images").file(png("camisa.png")))
            .andExpect(status().isBadGateway());
        assertThat(count()).isZero();
    }

    @Test
    void returnsSeveralImagesInTheRequestedOrder() throws Exception {
        String first = upload("a.png").get("id").asText();
        String second = upload("b.png").get("id").asText();

        mvc.perform(get("/images").param("ids", second, "no-existe", first))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(second))
            .andExpect(jsonPath("$[1].id").value(first));

        mvc.perform(get("/images")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/images/" + first)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("a.png"));
        mvc.perform(get("/images/no-existe")).andExpect(status().isNotFound());
    }

    @Test
    void renamesAnImage() throws Exception {
        String id = upload("IMG_2031.jpg").get("id").asText();

        mvc.perform(put("/images/" + id + "/name").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"camisa-1.jpg\"}"))
            .andExpect(status().isNoContent());
        mvc.perform(get("/images/" + id)).andExpect(jsonPath("$.name").value("camisa-1.jpg"));

        mvc.perform(put("/images/no-existe/name").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"x.jpg\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(put("/images/" + id + "/name").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void confirmsTheImagesInUse() throws Exception {
        JsonNode first = upload("a.png");
        JsonNode second = upload("b.png");

        mvc.perform(post("/images/confirm").contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\":[\"" + first.get("id").asText() + "\",\"" + second.get("id").asText() + "\"]}"))
            .andExpect(status().isNoContent());

        assertThat(statusOf(first)).isEqualTo("CONFIRMED");
        assertThat(statusOf(second)).isEqualTo("CONFIRMED");
    }

    @Test
    void saysWhichImagesCouldNotBeConfirmed() throws Exception {
        JsonNode image = upload("a.png");

        mvc.perform(post("/images/confirm").contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\":[\"" + image.get("id").asText() + "\",\"ya-no-existe\"]}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.missing[0]").value("ya-no-existe"));
    }

    @Test
    void deletesFromCloudinaryAndFromTheDatabase() throws Exception {
        JsonNode image = upload("a.png");

        mvc.perform(delete("/images/" + image.get("id").asText())).andExpect(status().isNoContent());

        verify(cloudinary).delete(image.get("imageId").asText());
        assertThat(count()).isZero();
        mvc.perform(delete("/images/no-existe")).andExpect(status().isNoContent());
    }

    @Test
    void keepsAFailedDeletionForTheCleanupAndHidesTheImage() throws Exception {
        JsonNode image = upload("a.png");
        String id = image.get("id").asText();
        doThrow(new IOException("cloudinary caída")).when(cloudinary).delete(anyString());

        mvc.perform(delete("/images/" + id)).andExpect(status().isNoContent());

        assertThat(statusOf(image)).isEqualTo("DELETING");
        mvc.perform(get("/images/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/images").param("ids", id)).andExpect(jsonPath("$.length()").value(0));

        reset(cloudinary);
        cleanup.cleanUp();

        verify(cloudinary).delete(image.get("imageId").asText());
        assertThat(count()).isZero();
    }

    @Test
    void cleanupDeletesOnlyOldImagesThatNobodyConfirmed() throws Exception {
        JsonNode abandoned = upload("abandonada.png");
        JsonNode recent = upload("reciente.png");
        JsonNode inUse = upload("en-uso.png");
        mvc.perform(post("/images/confirm").contentType(MediaType.APPLICATION_JSON)
            .content("{\"ids\":[\"" + inUse.get("id").asText() + "\"]}"));
        jdbc.update("UPDATE images SET created_at = NOW(6) - INTERVAL 2 DAY WHERE id IN (?, ?)",
            abandoned.get("id").asText(), inUse.get("id").asText());

        cleanup.cleanUp();

        verify(cloudinary).delete(abandoned.get("imageId").asText());
        verify(cloudinary, never()).delete(recent.get("imageId").asText());
        verify(cloudinary, never()).delete(inUse.get("imageId").asText());
        assertThat(jdbc.queryForList("SELECT name FROM images", String.class))
            .containsExactlyInAnyOrder("reciente.png", "en-uso.png");
    }

    private JsonNode upload(String filename) throws Exception {
        String body = mvc.perform(multipart("/images").file(png(filename)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    private String statusOf(JsonNode image) {
        return jdbc.queryForObject("SELECT status FROM images WHERE id = ?", String.class, image.get("id").asText());
    }

    private long count() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM images", Long.class);
    }

    private static MockMultipartFile png(String filename) {
        return new MockMultipartFile("file", filename, "image/png", new byte[] {1, 2, 3});
    }
}

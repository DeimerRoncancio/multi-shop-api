package com.multi.shop.identity.media;

import com.multi.shop.identity.FakeMediaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = MediaApiHttpClientTest.FeignOnly.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MediaApiHttpClientTest {
    private static final FakeMediaService MEDIA = new FakeMediaService();

    static {
        MEDIA.start();
    }

    @Configuration
    @EnableFeignClients(clients = MediaClient.class)
    @ImportAutoConfiguration({
        JacksonAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        FeignAutoConfiguration.class
    })
    @Import(MediaApiHttpClient.class)
    static class FeignOnly {
    }

    @DynamicPropertySource
    static void mediaService(DynamicPropertyRegistry registry) {
        registry.add("media.service.url", MEDIA::url);
    }

    @Autowired
    private MediaApiHttpClient client;

    @BeforeEach
    void resetMedia() {
        MEDIA.reset();
    }

    @AfterEach
    void endTransaction() {
        if (TransactionSynchronizationManager.isSynchronizationActive())
            TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void uploadsAndConfirmsJustBeforeTheSaveIsCommitted() throws IOException {
        TransactionSynchronizationManager.initSynchronization();

        StoredImage image = client.upload(photo("camisa.png"));

        assertThat(image.name()).isEqualTo("camisa.png");
        assertThat(image.imageId()).isEqualTo("foto-1");
        assertThat(status(image)).isEqualTo("PENDING");

        commit();
        assertThat(status(image)).isEqualTo("CONFIRMED");
        assertThat(MEDIA.deletedImageIds()).isEmpty();
    }

    @Test
    void deletesTheUploadWhenTheSaveIsRolledBack() throws IOException {
        TransactionSynchronizationManager.initSynchronization();

        StoredImage image = client.upload(photo("camisa.png"));
        rollback();

        assertThat(MEDIA.image(image.id())).isEmpty();
        assertThat(MEDIA.deletedImageIds()).containsExactly("foto-1");
    }

    @Test
    void stopsTheSaveWhenTheImageCannotBeConfirmed() throws IOException {
        TransactionSynchronizationManager.initSynchronization();
        client.upload(photo("camisa.png"));
        MEDIA.setRefuseConfirm(true);

        assertThatThrownBy(this::commit)
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("media service");
    }

    @Test
    void confirmsRightAwayWithoutATransaction() throws IOException {
        StoredImage image = client.upload(photo("camisa.png"));

        assertThat(status(image)).isEqualTo("CONFIRMED");
    }

    @Test
    void deletesOnlyAfterTheSaveIsCommitted() throws IOException {
        StoredImage image = client.upload(photo("camisa.png"));
        TransactionSynchronizationManager.initSynchronization();

        client.deleteAfterCommit(image.id());
        assertThat(MEDIA.image(image.id())).isPresent();

        commit();
        assertThat(MEDIA.image(image.id())).isEmpty();
    }

    @Test
    void keepsTheImageWhenTheSaveIsRolledBackAfterAskingToDeleteIt() throws IOException {
        StoredImage image = client.upload(photo("camisa.png"));
        TransactionSynchronizationManager.initSynchronization();

        client.deleteAfterCommit(image.id());
        rollback();

        assertThat(MEDIA.image(image.id())).isPresent();
    }

    @Test
    void readsAndRenamesImages() throws IOException {
        StoredImage first = client.upload(photo("a.png"));
        StoredImage second = client.upload(photo("b.png"));

        client.rename(first.id(), "camisa-1.png");

        assertThat(client.findAll(List.of(second.id(), first.id())))
            .extracting(StoredImage::name)
            .containsExactly("b.png", "camisa-1.png");
        assertThat(client.findOne(first.id())).map(StoredImage::name).contains("camisa-1.png");
        assertThat(client.findOne("no-existe")).isEmpty();
        assertThat(client.findOne(null)).isEmpty();
        assertThat(client.findAll(List.of())).isEmpty();
    }

    @Test
    void reportsAnUnavailableMediaService() {
        MEDIA.setDown(true);

        assertThatThrownBy(() -> client.upload(photo("camisa.png"))).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> client.findAll(List.of("x"))).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> client.findOne("x")).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> client.rename("x", "y")).isInstanceOf(ResponseStatusException.class);
    }

    private static String status(StoredImage image) {
        return MEDIA.image(image.id()).orElseThrow().status();
    }

    private static MockMultipartFile photo(String filename) {
        return new MockMultipartFile("file", filename, "image/png", new byte[] {1, 2, 3});
    }

    private void commit() {
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        try {
            synchronizations.forEach(synchronization -> synchronization.beforeCommit(false));
        } catch (RuntimeException exception) {
            synchronizations.forEach(synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
            throw exception;
        }
        synchronizations.forEach(TransactionSynchronization::afterCommit);
        synchronizations.forEach(synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
    }

    private void rollback() {
        TransactionSynchronizationManager.getSynchronizations()
            .forEach(synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
    }
}

package com.multi.shop.catalog.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MediaApiHttpClient implements MediaApi {
    private static final Logger log = LoggerFactory.getLogger(MediaApiHttpClient.class);
    private static final ParameterizedTypeReference<List<StoredImage>> IMAGE_LIST = new ParameterizedTypeReference<>() {};

    private final RestClient client;

    public MediaApiHttpClient(RestClient.Builder builder, @Value("${media.service.url}") String baseUrl) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        this.client = builder.clone()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
    }

    @Override
    public StoredImage upload(MultipartFile file) throws IOException {
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(file.getContentType() == null
            ? MediaType.APPLICATION_OCTET_STREAM
            : MediaType.parseMediaType(file.getContentType()));
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new HttpEntity<>(new NamedBytes(file.getBytes(), file.getOriginalFilename()), partHeaders));

        StoredImage image;
        try {
            image = client.post().uri("/images")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(StoredImage.class);
        } catch (RestClientException exception) {
            throw new IOException("The media service could not store the image", exception);
        }

        if (image == null) throw new IOException("The media service returned no image");
        confirmWhenSaved(image.id());
        return image;
    }

    @Override
    public Optional<StoredImage> findOne(String id) {
        if (id == null) return Optional.empty();

        try {
            return Optional.ofNullable(client.get().uri("/images/{id}", id).retrieve().body(StoredImage.class));
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        } catch (RestClientException exception) {
            throw unavailable("read the image " + id, exception);
        }
    }

    @Override
    public List<StoredImage> findAll(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        try {
            List<StoredImage> images = client.get()
                .uri(uri -> uri.path("/images").queryParam("ids", ids.toArray()).build())
                .retrieve()
                .body(IMAGE_LIST);
            return images == null ? List.of() : images;
        } catch (RestClientException exception) {
            throw unavailable("read " + ids.size() + " images", exception);
        }
    }

    @Override
    public void rename(String id, String name) {
        try {
            client.put().uri("/images/{id}/name", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("name", name))
                .retrieve()
                .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound exception) {
            log.warn("Image {} was not found to rename it", id);
        } catch (RestClientException exception) {
            throw unavailable("rename the image " + id, exception);
        }
    }

    @Override
    public void deleteAfterCommit(String id) {
        if (id == null) return;

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            delete(id);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                delete(id);
            }
        });
    }

    private void confirmWhenSaved(String id) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            confirm(id);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCommit(boolean readOnly) {
                confirm(id);
            }

            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) delete(id);
            }
        });
    }

    private void confirm(String id) {
        try {
            client.post().uri("/images/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("ids", List.of(id)))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientException exception) {
            throw unavailable("confirm the image " + id, exception);
        }
    }

    private void delete(String id) {
        try {
            client.delete().uri("/images/{id}", id).retrieve().toBodilessEntity();
        } catch (RestClientException exception) {
            log.warn("Could not delete image {} in the media service: {}", id, exception.getMessage());
        }
    }

    private static ResponseStatusException unavailable(String action, RestClientException exception) {
        log.warn("The media service could not {}: {}", action, exception.getMessage());
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The media service is not available");
    }

    private static final class NamedBytes extends ByteArrayResource {
        private final String filename;

        private NamedBytes(byte[] bytes, String filename) {
            super(bytes);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}

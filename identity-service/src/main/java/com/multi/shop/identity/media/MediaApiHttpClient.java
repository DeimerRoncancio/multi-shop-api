package com.multi.shop.identity.media;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MediaApiHttpClient implements MediaApi {
    private static final Logger log = LoggerFactory.getLogger(MediaApiHttpClient.class);

    private final MediaClient client;

    public MediaApiHttpClient(MediaClient client) {
        this.client = client;
    }

    @Override
    public StoredImage upload(MultipartFile file) throws IOException {
        StoredImage image;
        try {
            image = client.upload(file);
        } catch (FeignException exception) {
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
            return Optional.ofNullable(client.findOne(id));
        } catch (FeignException.NotFound exception) {
            return Optional.empty();
        } catch (FeignException exception) {
            throw unavailable("read the image " + id, exception);
        }
    }

    @Override
    public List<StoredImage> findAll(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        try {
            List<StoredImage> images = client.findAll(List.copyOf(ids));
            return images == null ? List.of() : images;
        } catch (FeignException exception) {
            throw unavailable("read " + ids.size() + " images", exception);
        }
    }

    @Override
    public void rename(String id, String name) {
        try {
            client.rename(id, Map.of("name", name));
        } catch (FeignException.NotFound exception) {
            log.warn("Image {} was not found to rename it", id);
        } catch (FeignException exception) {
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
            client.confirm(Map.of("ids", List.of(id)));
        } catch (FeignException exception) {
            throw unavailable("confirm the image " + id, exception);
        }
    }

    private void delete(String id) {
        try {
            client.delete(id);
        } catch (FeignException exception) {
            log.warn("Could not delete image {} in the media service: {}", id, exception.getMessage());
        }
    }

    private static ResponseStatusException unavailable(String action, FeignException exception) {
        log.warn("The media service could not {}: {}", action, exception.getMessage());
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The media service is not available");
    }
}

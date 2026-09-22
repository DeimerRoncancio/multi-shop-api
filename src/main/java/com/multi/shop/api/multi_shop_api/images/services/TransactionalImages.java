package com.multi.shop.api.multi_shop_api.images.services;

import com.multi.shop.api.multi_shop_api.images.entities.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;

@Component
public class TransactionalImages {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalImages.class);

    private final ImageService imageService;

    public TransactionalImages(ImageService imageService) {
        this.imageService = imageService;
    }

    public void deleteAfterCommit(Image image) {
        if (image == null) return;

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            delete(image);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                delete(image);
            }
        });
    }

    public void deleteOnRollback(Image image) {
        if (image == null || !TransactionSynchronizationManager.isSynchronizationActive()) return;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) delete(image);
            }
        });
    }

    private void delete(Image image) {
        try {
            imageService.deleteImage(image);
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Exception trying to delete image {}: {}", image.getImageId(), exception.getMessage());
        }
    }
}

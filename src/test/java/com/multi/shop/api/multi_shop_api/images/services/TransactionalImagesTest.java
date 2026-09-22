package com.multi.shop.api.multi_shop_api.images.services;

import com.multi.shop.api.multi_shop_api.images.entities.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class TransactionalImagesTest {
    private final ImageService imageService = mock(ImageService.class);
    private final TransactionalImages transactionalImages = new TransactionalImages(imageService);
    private final Image image = new Image();

    @AfterEach
    void endTransaction() {
        if (TransactionSynchronizationManager.isSynchronizationActive())
            TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void doesNotDeleteFromCloudinaryUntilTheTransactionCommits() throws IOException {
        TransactionSynchronizationManager.initSynchronization();

        transactionalImages.deleteAfterCommit(image);
        verifyNoInteractions(imageService);

        commit();
        verify(imageService).deleteImage(image);
    }

    @Test
    void keepsTheImageWhenTheTransactionIsRolledBack() throws IOException {
        TransactionSynchronizationManager.initSynchronization();

        transactionalImages.deleteAfterCommit(image);
        rollback();

        verify(imageService, never()).deleteImage(image);
    }

    @Test
    void deletesAnUploadedImageWhenTheTransactionIsRolledBack() throws IOException {
        TransactionSynchronizationManager.initSynchronization();

        transactionalImages.deleteOnRollback(image);
        verifyNoInteractions(imageService);

        rollback();
        verify(imageService).deleteImage(image);
    }

    @Test
    void keepsAnUploadedImageWhenTheTransactionCommits() throws IOException {
        TransactionSynchronizationManager.initSynchronization();

        transactionalImages.deleteOnRollback(image);
        commit();

        verify(imageService, never()).deleteImage(image);
    }

    @Test
    void deletesRightAwayWithoutATransaction() throws IOException {
        transactionalImages.deleteAfterCommit(image);

        verify(imageService).deleteImage(image);
    }

    @Test
    void ignoresAnImageThatDoesNotExist() {
        transactionalImages.deleteAfterCommit(null);
        transactionalImages.deleteOnRollback(null);

        verifyNoInteractions(imageService);
    }

    @Test
    void keepsWorkingWhenCloudinaryFails() throws IOException {
        org.mockito.Mockito.doThrow(new IOException("cloudinary caída")).when(imageService).deleteImage(image);

        transactionalImages.deleteAfterCommit(image);

        verify(imageService).deleteImage(image);
    }

    private void commit() {
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        completion(TransactionSynchronization.STATUS_COMMITTED);
    }

    private void rollback() {
        completion(TransactionSynchronization.STATUS_ROLLED_BACK);
    }

    private void completion(int status) {
        TransactionSynchronizationManager.getSynchronizations()
            .forEach(synchronization -> synchronization.afterCompletion(status));
    }
}

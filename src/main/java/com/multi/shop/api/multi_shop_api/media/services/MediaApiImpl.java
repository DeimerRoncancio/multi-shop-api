package com.multi.shop.api.multi_shop_api.media.services;

import com.multi.shop.api.multi_shop_api.media.api.MediaApi;
import com.multi.shop.api.multi_shop_api.media.api.StoredImage;
import com.multi.shop.api.multi_shop_api.media.entities.Image;
import com.multi.shop.api.multi_shop_api.media.repositories.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MediaApiImpl implements MediaApi {
    private final ImageRepository repository;
    private final ImageService imageService;
    private final TransactionalImages transactionalImages;

    public MediaApiImpl(ImageRepository repository, ImageService imageService, TransactionalImages transactionalImages) {
        this.repository = repository;
        this.imageService = imageService;
        this.transactionalImages = transactionalImages;
    }

    @Override
    @Transactional
    public StoredImage upload(MultipartFile file) throws IOException {
        Image image = imageService.uploadImage(file);
        transactionalImages.deleteOnRollback(image);
        return toStoredImage(image);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoredImage> findOne(String id) {
        if (id == null) return Optional.empty();
        return repository.findById(id).map(MediaApiImpl::toStoredImage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoredImage> findAll(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        Map<String, Image> images = repository.findAllById(ids).stream()
            .collect(Collectors.toMap(Image::getId, Function.identity()));

        return ids.stream()
            .map(images::get)
            .filter(Objects::nonNull)
            .map(MediaApiImpl::toStoredImage)
            .toList();
    }

    @Override
    @Transactional
    public void rename(String id, String name) {
        repository.findById(id).ifPresent(image -> image.setName(name));
    }

    @Override
    @Transactional
    public void deleteAfterCommit(String id) {
        if (id == null) return;
        repository.findById(id).ifPresent(transactionalImages::deleteAfterCommit);
    }

    private static StoredImage toStoredImage(Image image) {
        return new StoredImage(image.getId(), image.getName(), image.getImageUrl(), image.getImageId());
    }
}

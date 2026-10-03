package com.multi.shop.media.images;

import com.multi.shop.media.cloudinary.CloudinaryService;
import com.multi.shop.media.cloudinary.CloudinaryService.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Clock;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ImageService {
    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final ImageRepository repository;
    private final CloudinaryService cloudinary;
    private final Clock clock;

    public ImageService(ImageRepository repository, CloudinaryService cloudinary, Clock clock) {
        this.repository = repository;
        this.cloudinary = cloudinary;
        this.clock = clock;
    }

    public ImageResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The file is empty");

        UploadedFile uploaded;
        try {
            uploaded = cloudinary.upload(file.getBytes());
        } catch (IOException | RuntimeException exception) {
            log.warn("Could not upload {} to Cloudinary: {}", file.getOriginalFilename(), exception.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The image could not be uploaded");
        }

        Image image = new Image(file.getOriginalFilename(), uploaded.url(), uploaded.publicId(), clock.instant());
        return ImageResponse.of(repository.save(image));
    }

    @Transactional(readOnly = true)
    public Optional<ImageResponse> findOne(String id) {
        return repository.findById(id)
            .filter(image -> image.getStatus() != ImageStatus.DELETING)
            .map(ImageResponse::of);
    }

    @Transactional(readOnly = true)
    public List<ImageResponse> findAll(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        Map<String, Image> images = repository.findByIdInAndStatusNot(new LinkedHashSet<>(ids), ImageStatus.DELETING).stream()
            .collect(Collectors.toMap(Image::getId, Function.identity()));

        return ids.stream()
            .map(images::get)
            .filter(Objects::nonNull)
            .map(ImageResponse::of)
            .toList();
    }

    @Transactional
    public boolean rename(String id, String name) {
        return repository.findById(id)
            .filter(image -> image.getStatus() != ImageStatus.DELETING)
            .map(image -> {
                image.setName(name);
                return true;
            })
            .orElse(false);
    }

    @Transactional
    public Set<String> confirm(Collection<String> ids) {
        Set<String> wanted = new LinkedHashSet<>(ids);
        List<Image> images = repository.findByIdInAndStatusNot(wanted, ImageStatus.DELETING);
        images.forEach(image -> image.setStatus(ImageStatus.CONFIRMED));

        Set<String> missing = new LinkedHashSet<>(wanted);
        images.forEach(image -> missing.remove(image.getId()));
        return missing;
    }

    public void delete(String id) {
        repository.findById(id).ifPresent(this::delete);
    }

    void delete(Image image) {
        if (image.getStatus() != ImageStatus.DELETING) {
            image.setStatus(ImageStatus.DELETING);
            repository.save(image);
        }

        try {
            cloudinary.delete(image.getImageId());
            repository.delete(image);
        } catch (IOException | RuntimeException exception) {
            log.warn("Could not delete {} from Cloudinary, the cleanup will retry: {}", image.getImageId(), exception.getMessage());
        }
    }
}

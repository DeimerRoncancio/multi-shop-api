package com.multi.shop.media.images;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, String> {
    List<Image> findByIdInAndStatusNot(Collection<String> ids, ImageStatus status);

    List<Image> findByStatusAndCreatedAtBefore(ImageStatus status, Instant limit);

    List<Image> findByStatus(ImageStatus status);
}

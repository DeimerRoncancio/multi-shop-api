package com.multi.shop.catalog.media;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MediaApi {
    StoredImage upload(MultipartFile file) throws IOException;

    Optional<StoredImage> findOne(String id);

    List<StoredImage> findAll(Collection<String> ids);

    void rename(String id, String name);

    void deleteAfterCommit(String id);
}

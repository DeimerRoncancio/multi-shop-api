package com.multi.shop.api.multi_shop_api.images.services;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import com.multi.shop.api.multi_shop_api.images.entities.Image;

public interface ImageService {
    Image uploadImage(MultipartFile multipartFile) throws IOException;

    void deleteImage(Image image) throws IOException;
}

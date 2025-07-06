package com.multi.shop.api.multi_shop_api.cloudinary;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    @SuppressWarnings("rawtypes")
    Map upload(MultipartFile multipartFile) throws IOException;
    
    @SuppressWarnings("rawtypes")
    Map delete(String id) throws IOException;
}

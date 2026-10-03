package com.multi.shop.media.cloudinary;

import java.io.IOException;

public interface CloudinaryService {
    UploadedFile upload(byte[] content) throws IOException;

    void delete(String publicId) throws IOException;

    record UploadedFile(String url, String publicId) {
    }
}

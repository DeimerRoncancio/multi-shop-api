package com.multi.shop.media.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(
        @Value("${cloudinary.cloud-name}") String cloudName,
        @Value("${cloudinary.api-key}") String apiKey,
        @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        cloudinary = new Cloudinary(Map.of(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        ));
    }

    @Override
    @SuppressWarnings("rawtypes")
    public UploadedFile upload(byte[] content) throws IOException {
        Map result = cloudinary.uploader().upload(content, ObjectUtils.emptyMap());
        return new UploadedFile((String) result.get("url"), (String) result.get("public_id"));
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void delete(String publicId) throws IOException {
        Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        Object outcome = result.get("result");
        if (!"ok".equals(outcome) && !"not found".equals(outcome))
            throw new IOException("Cloudinary did not delete " + publicId + ": " + outcome);
    }
}

package com.multi.shop.api.multi_shop_api.services;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.entities.Image;
import com.multi.shop.api.multi_shop_api.repositories.ImageRepository;

@Service
public class ImageServiceImpl implements ImageService {
    
    private final ImageRepository repository;
    private final CloudinaryService cloudinaryService;

    public ImageServiceImpl(ImageRepository repository, CloudinaryService cloudinaryService) {
        this.repository = repository;
        this.cloudinaryService = cloudinaryService;
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public Image uploadImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinaryService.upload(file);
        String url = (String) uploadResult.get("url");
        String imageId = (String) uploadResult.get("public_id");
        Image image = new Image(file.getOriginalFilename(), url, imageId);
        return repository.save(image);
    }

    @Override
    public void deleteImage(Image image) throws IOException {
        cloudinaryService.delete(image.getImageId());
        repository.deleteById(image.getId());
    }
}

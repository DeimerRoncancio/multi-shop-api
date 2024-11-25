package com.majestic.food.api.majestic_food_api.services;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.majestic.food.api.majestic_food_api.entities.Image;

public interface ImageService {

    Image uploadImage(MultipartFile multipartFile) throws IOException;

    void deleteImage(Image id) throws IOException;
}

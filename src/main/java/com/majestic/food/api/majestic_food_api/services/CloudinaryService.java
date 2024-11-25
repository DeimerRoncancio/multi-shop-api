package com.majestic.food.api.majestic_food_api.services;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    @SuppressWarnings("rawtypes")
    Map upload(MultipartFile miltipartFile) throws IOException;
    
    @SuppressWarnings("rawtypes")
    Map delete(String id) throws IOException;
}

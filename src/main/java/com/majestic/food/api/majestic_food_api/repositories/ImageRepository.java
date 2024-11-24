package com.majestic.food.api.majestic_food_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.majestic.food.api.majestic_food_api.entities.Image;

public interface ImageRepository extends JpaRepository<Image, String>{
}

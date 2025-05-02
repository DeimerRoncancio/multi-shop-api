package com.multi.shop.api.multi_shop_api.images.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.multi.shop.api.multi_shop_api.images.entities.Image;

public interface ImageRepository extends JpaRepository<Image, String>{
}

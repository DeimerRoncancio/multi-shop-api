package com.multi.shop.api.multi_shop_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.multi.shop.api.multi_shop_api.entities.Image;

public interface ImageRepository extends JpaRepository<Image, String>{
}

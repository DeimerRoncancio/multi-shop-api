package com.multi.shop.api.multi_shop_api.media.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.multi.shop.api.multi_shop_api.media.entities.Image;

public interface ImageRepository extends JpaRepository<Image, String>{
}

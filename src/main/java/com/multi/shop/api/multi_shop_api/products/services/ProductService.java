package com.multi.shop.api.multi_shop_api.products.services;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.entities.dtos.NewProductDTO;
import com.multi.shop.api.multi_shop_api.products.entities.dtos.UpdateProductDTO;

public interface ProductService {
    List<Product> findAll();

    Optional<Product> findOne(String id);

    NewProductDTO save(NewProductDTO product, List<MultipartFile> files);

    Optional<Product> update(String id, UpdateProductDTO product, List<MultipartFile> files);

    Optional<Product> delete(String id);

    int productsSize();
}

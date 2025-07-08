package com.multi.shop.api.multi_shop_api.products.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.dtos.NewProductDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.UpdateProductDTO;

public interface ProductService {
    Page<Product> findAll(Pageable pageable);

    Optional<Product> findOne(String id);

    NewProductDTO save(NewProductDTO product, List<MultipartFile> files);

    Optional<Product> update(String id, UpdateProductDTO product, List<MultipartFile> files);

    Optional<Product> delete(String id);

    int productsSize();
}

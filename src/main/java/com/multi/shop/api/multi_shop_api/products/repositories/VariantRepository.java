package com.multi.shop.api.multi_shop_api.products.repositories;

import com.multi.shop.api.multi_shop_api.products.entities.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariantRepository extends JpaRepository<Variant, String> {
    List<Variant> findByNameIn(List<String> variants);
}

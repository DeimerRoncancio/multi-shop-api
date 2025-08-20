package com.multi.shop.api.multi_shop_api.products.repositories;

import com.multi.shop.api.multi_shop_api.products.entities.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantRepository extends JpaRepository<Variant, String> {
}

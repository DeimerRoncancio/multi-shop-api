package com.multi.shop.api.multi_shop_api.products.services;

import com.multi.shop.api.multi_shop_api.products.dtos.VariantDTO;
import com.multi.shop.api.multi_shop_api.products.entities.Variant;

import java.util.List;
import java.util.Optional;

public interface VariantService {
    List<Variant> findAll();
    VariantDTO addVariant(VariantDTO newVariant);
}

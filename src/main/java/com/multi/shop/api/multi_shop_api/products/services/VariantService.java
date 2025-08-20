package com.multi.shop.api.multi_shop_api.products.services;

import com.multi.shop.api.multi_shop_api.products.dtos.VariantDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.VariantResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface VariantService {
    Page<VariantResponseDTO> findAll(Pageable pageable);
    VariantResponseDTO findOne(String id);
    VariantDTO addVariant(VariantDTO newVariant);
}

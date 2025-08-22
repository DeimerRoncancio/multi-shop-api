package com.multi.shop.api.multi_shop_api.products.services;

import com.multi.shop.api.multi_shop_api.products.dtos.VariantDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.VariantResponseDTO;
import com.multi.shop.api.multi_shop_api.products.entities.Variant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface VariantService {
    Page<VariantResponseDTO> findAll(Pageable pageable);
    VariantResponseDTO findOne(String id);
    VariantDTO addVariant(VariantDTO newVariant);
    Optional<VariantDTO> updateVariant(String id, VariantDTO variant);
    Optional<Variant> deleteVariant(String id);
    List<Variant> findVariantsByName(List<String> tag);
}

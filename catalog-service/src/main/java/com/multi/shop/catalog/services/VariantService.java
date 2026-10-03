package com.multi.shop.catalog.services;

import com.multi.shop.catalog.dtos.VariantDTO;
import com.multi.shop.catalog.dtos.VariantResponseDTO;
import com.multi.shop.catalog.entities.Variant;
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

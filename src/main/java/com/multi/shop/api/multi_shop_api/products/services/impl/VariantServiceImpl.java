package com.multi.shop.api.multi_shop_api.products.services.impl;

import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import com.multi.shop.api.multi_shop_api.products.dtos.VariantDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.VariantResponseDTO;
import com.multi.shop.api.multi_shop_api.products.entities.Variant;
import com.multi.shop.api.multi_shop_api.products.mappers.VariantMapper;
import com.multi.shop.api.multi_shop_api.products.repositories.VariantRepository;
import com.multi.shop.api.multi_shop_api.products.services.VariantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VariantServiceImpl implements VariantService {
    private final VariantRepository repository;

    public VariantServiceImpl(VariantRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VariantResponseDTO> findAll(Pageable pageable){
        Page<Variant> variants = repository.findAll(pageable);

        return variants.map(var -> {
            List<String> values = List.of(var.getValues().split("\\|"));
            return VariantMapper.MAPPER.variantToDTO(var, values);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public VariantResponseDTO findOne(String id) {
        Variant variant = repository.findById(id).orElseThrow(() -> new NotFoundException("Variant not found"));
        List<String> values = List.of(variant.getValues().split("\\|"));

        return VariantMapper.MAPPER.variantToDTO(variant, values);
    }

    @Override
    @Transactional
    public VariantDTO addVariant(VariantDTO newVariant) {
        String values = String.join("|", newVariant.listValues());
        repository.save(VariantMapper.MAPPER.dtoToVariant(newVariant, values));
        return newVariant;
    }

    @Override
    @Transactional
    public Optional<VariantDTO> updateVariant(String id, VariantDTO variantDTO){
        return repository.findById(id).map(variantDb -> {
            String values = String.join("|", variantDTO.listValues());

            variantDb.setName(variantDTO.name());
            variantDb.setTag(variantDTO.tag());
            variantDb.setValues(values);
            repository.save(variantDb);

            return variantDTO;
        });
    }
}

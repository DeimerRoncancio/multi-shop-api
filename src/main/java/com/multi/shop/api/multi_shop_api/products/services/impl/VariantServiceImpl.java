package com.multi.shop.api.multi_shop_api.products.services.impl;

import com.multi.shop.api.multi_shop_api.products.dtos.VariantDTO;
import com.multi.shop.api.multi_shop_api.products.entities.Variant;
import com.multi.shop.api.multi_shop_api.products.mappers.VariantMapper;
import com.multi.shop.api.multi_shop_api.products.repositories.VariantRepository;
import com.multi.shop.api.multi_shop_api.products.services.VariantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VariantServiceImpl implements VariantService {
    private final VariantRepository repository;

    public VariantServiceImpl(VariantRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Variant> findAll(){
        return repository.findAll();
    }

    @Override
    @Transactional
    public VariantDTO addVariant(VariantDTO newVariant) {
        String values = String.join("|", newVariant.listValues());
        Variant variant = VariantMapper.MAPPER.variantToDTO(newVariant, values);
        repository.save(variant);
        return newVariant;
    }
}
